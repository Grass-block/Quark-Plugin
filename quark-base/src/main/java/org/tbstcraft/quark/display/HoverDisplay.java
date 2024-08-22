package org.tbstcraft.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.tbstcraft.quark.foundation.command.*;
import org.tbstcraft.quark.foundation.command.execute.CommandExecution;
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
public class HoverDisplay extends PackageModule implements org.tbstcraft.quark.foundation.command.execute.CommandExecutor {
    private final Map<String, ArmorStand> stands = new HashMap<>();

    @Override
    public void enable() {
        super.enable();
    }

    public void clearAll() {
        TaskService.runTask(() -> {
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
        });
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

        switch (context.requireEnum(0, "create", "clear-all", "clear")) {
            case "create" -> {
                create(context.requireArgumentAt(1), sender.getLocation(), TextBuilder.buildComponent(context.requireArgumentAt(2)));
            }
            case "clear-all" -> {
                this.clearAll();
            }
            case "clear" -> {

            }
        }


    }


    @QuarkCommand(name = "hover-display", permission = "-quark.hoverdisplay")
    public static final class HoverDisplayCommand extends ModuleCommand<HoverDisplay> {
        @Override
        public void init(HoverDisplay module) {
            this.setExecutor(module);
        }
    }
}
