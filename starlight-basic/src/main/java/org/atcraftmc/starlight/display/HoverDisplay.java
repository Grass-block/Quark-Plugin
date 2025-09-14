package org.atcraftmc.starlight.display;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.nbt.NBTTagList;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO1;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.custom.CustomMeta;
import org.atcraftmc.starlight.data.ModuleDataService;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.BukkitCodec;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.Registers;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;

@SLModule
@AutoRegister(Registers.BUKKIT_EVENT)
@CommandProvider(HoverDisplay.HoverDisplayCommand.class)
public final class HoverDisplay extends PackageModule implements PluginCommandExecutor {
    private final Map<String, ArmorStandGroup> stands = new HashMap<>();

    @Inject
    private Logger logger;

    @Inject
    private LanguageEntry language;

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireClass(() -> Class.forName("org.bukkit.Nameable"));
        Compatibility.requireMethod(() -> Nameable.class.getDeclaredMethod("customName"));
    }

    @Override
    public void enable() {
        NBTTagCompound entry = ModuleDataService.getEntry(this.getFullId());

        entry.getTagMap().forEach((k, v) -> {
            var location = BukkitCodec.location(((NBTTagCompound) v).getCompoundTag("location"));
            var texts = new ArrayList<Component>();

            if (!((NBTTagCompound) v).hasKey("texts")) {
                texts.add(ComponentSerializer.json(((NBTTagCompound) v).getString("text")));
            } else {
                var list = ((NBTTagCompound) v).getTagList("texts");

                for (int i = 0; i < list.size(); i++) {
                    texts.add(ComponentSerializer.json(list.getString(i)));
                }
            }

            var group = new ArmorStandGroup(location, texts);

            this.stands.put(k, group);
        });

        this.logger.info("created %s texts".formatted(stands.size()));
    }

    @Override
    public void disable() {
        NBTTagCompound entry = ModuleDataService.getEntry(this.getFullId());

        entry.getTagMap().clear();

        this.stands.forEach((id, s) -> {
            NBTTagCompound tag = new NBTTagCompound();
            var texts = new NBTTagList();

            for (var text : s.texts) {
                texts.addString(ComponentSerializer.json(text));
            }

            tag.setTag("texts", texts);
            tag.setCompoundTag("location", BukkitCodec.nbt(s.anchor));

            entry.setCompoundTag(id, tag);
        });

        ModuleDataService.save(this.getFullId());
        this.stands.clear();
        TaskService.global().run(this::clearAll);
    }

    public void clearAll() {
        for (ArmorStandGroup s : stands.values()) {
            s.destroy();
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

    public void create(String id, Location loc, List<Component> text) {
        var group = new ArmorStandGroup(loc, text);
        this.stands.put(id, group);
    }


    @Override
    public void execute(CommandExecution context) {
        var sender = context.requireSenderAsPlayer();

        var op = context.requireEnum(0, "create", "delete-all", "delete", "edit", "tp");

        if (Objects.equals(op, "delete-all")) {
            TaskService.global().run(this::clearAll);
            MessageAccessor.send(this.language, sender, "delete-all");
            return;
        }

        var name = context.requireArgumentAt(1);

        if (Objects.equals(op, "create")) {
            if (stands.containsKey(name)) {
                MessageAccessor.send(this.language, sender, "exist", name);
                return;
            }
        } else if (!stands.containsKey(name)) {
            MessageAccessor.send(this.language, sender, "not-found", name);
            return;
        }


        switch (op) {
            case "create" -> {
                create(name, sender.getLocation().add(0, 1.37, 0), buildText(context));
                MessageAccessor.send(this.language, sender, "create", name);
            }
            case "delete" -> {
                TaskService.global().run(() -> stands.remove(name).destroy());
                MessageAccessor.send(this.language, sender, "delete", name);
            }
            case "edit" -> {
                this.stands.get(name).edit(buildText(context));
                MessageAccessor.send(this.language, sender, "edit", name);
            }
            case "tp" -> {
                stands.get(name).teleport(sender.getLocation().add(0, 1.37, 0));
                MessageAccessor.send(this.language, sender, "teleport", name);
            }
        }
    }

    private List<Component> buildText(CommandExecution context) {
        return Arrays.stream(context.requireRemainAsParagraph(2, true).split("\\{#return}")).map(TextBuilder::buildComponent).toList();
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

    public static final class ArmorStandGroup {
        public static final MethodHandleO1<ArmorStand, Component> CUSTOM_NAME = MethodHandle.select((ctx) -> {
            ctx.attempt(() -> Nameable.class.getMethod("customName", Component.class), (p, c) -> p.customName(c));
            ctx.dummy((a, c) -> a.setCustomName(ComponentSerializer.legacy(c)));
        });

        private final Set<ArmorStand> components = new HashSet<>();
        private final List<Component> texts = new ArrayList<>();
        private Location anchor;

        public ArmorStandGroup(Location anchor, List<Component> texts) {
            this.anchor = anchor;
            this.texts.addAll(texts);
            regenerate();
        }

        private void destroy() {
            for (ArmorStand s : this.components) {
                s.remove();
            }
            this.components.clear();
        }

        private void regenerate() {
            this.destroy();

            for (int i = 0; i < this.texts.size(); i++) {
                var text = this.texts.get(i);
                var location = this.anchor.clone().subtract(0, 0.244 * i, 0);

                create(location, text);
            }
        }

        public void edit(List<Component> texts) {
            this.destroy();
            this.texts.clear();
            this.texts.addAll(texts);
            regenerate();
        }

        public void teleport(Location location) {
            this.destroy();
            this.anchor = location;
            regenerate();
        }

        public void create(Location loc, Component text) {
            var stand = loc.getWorld().spawn(loc, ArmorStand.class);
            CustomMeta.setPDCIdentifier(stand, "quark:hover-text");

            stand.teleport(loc);

            stand.setMarker(true);
            stand.setSmall(true);
            stand.setGravity(false);
            stand.setInvulnerable(true);
            stand.setVisible(false);
            CUSTOM_NAME.invoke(stand, text);
            stand.setCustomNameVisible(true);

            this.components.add(stand);
        }
    }
}
