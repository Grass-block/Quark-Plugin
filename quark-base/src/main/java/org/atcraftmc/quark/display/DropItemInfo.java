package org.atcraftmc.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import net.kyori.adventure.text.Component;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Nameable;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

@QuarkModule
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class DropItemInfo extends PackageModule {

    @Override
    public void enable() {
        for (var w : Bukkit.getWorlds()) {
            discover(w);
        }
    }

    @Override
    public void disable() {
        for (var w : Bukkit.getWorlds()) {
            for (var e : w.getEntities()) {
                if (e instanceof Item i) {
                    i.setCustomName(null);
                    i.setCustomNameVisible(false);
                }
            }
        }
    }

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireMethod(() -> Nameable.class.getMethod("customName", Component.class));
    }

    @EventHandler
    public void onDropItem(final PlayerDropItemEvent event) {
        setId(event.getItemDrop());
    }

    @EventHandler
    public void onBlockDropItem(final BlockDropItemEvent event) {
        for (var i : event.getItems()) {
            setId(i);
        }
    }

    @EventHandler
    public void onEntityDropItem(final EntityDropItemEvent event) {
        setId(event.getItemDrop());
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent event) {
        var entity = event.getEntity();

        TaskService.entity(entity).delay(1, () -> {
            for (var e : entity.getNearbyEntities(10, 10, 10)) {
                if (e instanceof Item i) {
                    setId(i);
                }
            }
        });
    }

    @EventHandler
    public void onEntityDeath(final PlayerDeathEvent event) {
        var entity = event.getEntity();

        TaskService.entity(entity).delay(1, () -> {
            for (var e : entity.getNearbyEntities(10, 10, 10)) {
                if (e instanceof Item i) {
                    setId(i);
                }
            }
        });
    }


    @EventHandler
    public void onItemMerge(final ItemMergeEvent event) {
        TaskService.entity(event.getTarget()).delay(1, () -> setId(event.getTarget()));
    }

    public void discover(World world) {
        for (var e : world.getEntities()) {
            if (e instanceof Item i) {
                setId(i);
            }
        }
    }

    public void setId(Item item) {
        var template = "{#red}{amount}{#yellow}x{translate;color(aqua)}{id}{;}";

        var stack = item.getItemStack();
        var type = stack.getType();
        var id = (type.isBlock() ? "block." : "item.") + type.getKey().toString().replace(":", ".");

        item.customName(TextBuilder.buildComponent(template.replace("{amount}", String.valueOf(stack.getAmount())).replace("{id}", id)));
        item.setCustomNameVisible(true);
    }
}
