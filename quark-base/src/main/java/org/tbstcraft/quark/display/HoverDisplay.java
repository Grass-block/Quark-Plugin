package org.tbstcraft.quark.display;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.tbstcraft.quark.data.ModuleDataService;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.command.execute.CommandExecution;
import org.tbstcraft.quark.foundation.command.execute.CommandExecutor;
import org.tbstcraft.quark.foundation.command.execute.CommandSuggestion;
import org.tbstcraft.quark.foundation.platform.BukkitCodec;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.foundation.text.ComponentSerializer;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.framework.customcontent.CustomMeta;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@QuarkModule(version = "1.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(HoverDisplay.HoverDisplayCommand.class)
public final class HoverDisplay extends PackageModule implements CommandExecutor {
    private final Map<String, ArmorStand> stands = new HashMap<>();

    @Override
    public void enable() {
        NBTTagCompound entry = ModuleDataService.getEntry(this.getFullId());

        entry.getTagMap().forEach((k, v) -> {
            var text = ComponentSerializer.json(((NBTTagCompound) v).getString("text"));
            var location = BukkitCodec.location(((NBTTagCompound) v).getCompoundTag("location"));

            create(k, location, text);
        });

        getLogger().info("created %s texts".formatted(stands.size()));
    }

    @Override
    public void disable() {
        NBTTagCompound entry = ModuleDataService.getEntry(this.getFullId());

        entry.getTagMap().clear();

        this.stands.forEach((id, s) -> {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("text", ComponentSerializer.json(Objects.requireNonNull(s.customName())));
            tag.setCompoundTag("location", BukkitCodec.nbt(s.getLocation()));

            entry.setCompoundTag(id, tag);
        });

        ModuleDataService.save(this.getFullId());

        this.stands.clear();

        TaskService.runTask(this::clearAll);
    }

    public void clearAll() {
        for (ArmorStand s : stands.values()) {
            s.remove();
        }

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {

                if (entity.getType() != EntityType.ARMOR_STAND) {
                    continue;
                }

                if (!CustomMeta.hasPDCIdentifier(entity)) {
                    continue;
                }

                if (!Objects.equals(CustomMeta.getPDCIdentifier(entity), "quark:hover-text")) {
                    continue;
                }

                entity.remove();
            }
        }
    }

    public void create(String id, Location loc, Component text) {
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
        CustomMeta.setPDCIdentifier(stand, "quark:hover-text");

        stand.teleport(loc);

        stand.setMarker(true);
        stand.setSmall(true);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setVisible(false);
        stand.customName(text);
        stand.setCustomNameVisible(true);

        this.stands.put(id, stand);
    }


    @Override
    public void execute(CommandExecution context) {
        var sender = context.requireSenderAsPlayer();

        var op = context.requireEnum(0, "create", "delete-all", "delete", "edit", "tp");

        if (Objects.equals(op, "delete-all")) {
            TaskService.runTask(this::clearAll);
            getLanguage().sendMessage(sender, "delete-all");
            return;
        }

        var name = context.requireArgumentAt(1);

        if (Objects.equals(op, "create")) {
            if (stands.containsKey(name)) {
                getLanguage().sendMessage(sender, "exist", name);
                return;
            }
        } else if (!stands.containsKey(name)) {
            getLanguage().sendMessage(sender, "not-found", name);
            return;
        }


        switch (op) {
            case "create" -> {
                create(name, sender.getLocation(), TextBuilder.buildComponent(context.requireRemainAsParagraph(2, true)));
                getLanguage().sendMessage(sender, "create", name);
            }
            case "delete" -> {
                TaskService.runTask(() -> stands.remove(name).remove());

                getLanguage().sendMessage(sender, "delete", name);
            }
            case "edit" -> {
                stands.get(name).customName(TextBuilder.buildComponent(context.requireRemainAsParagraph(2, true)));
                getLanguage().sendMessage(sender, "edit", name);
            }
            case "tp" -> {
                Players.teleport(stands.get(name), context.requireSenderAsPlayer().getLocation());
                getLanguage().sendMessage(sender, "tp", name);
            }
        }
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "create", "delete", "edit", "tp");
        suggestion.suggest(1, stands.keySet());
    }

    @QuarkCommand(name = "hover-display", permission = "-quark.hoverdisplay")
    public static final class HoverDisplayCommand extends ModuleCommand<HoverDisplay> {
        @Override
        public void init(HoverDisplay module) {
            this.setExecutor(module);
        }
    }
}
