package org.atcraftmc.starlight.display;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import net.kyori.adventure.text.Component;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.api.event.ItemCreateEvent;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
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

import java.util.Objects;

@ApplicationModule(id = "drop-item-info")
@AutoRegister(Registrations.SERVER_EVENT)
public final class DropItemInfo extends SLPackageModule {

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
    public void onDropCreate(ItemCreateEvent event){
        setId(event.getItem());
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
        var template = config().value("template").string();

        var stack = item.getItemStack();
        var type = stack.getType();
        var id = (type.isBlock() ? "block." : "item.") + type.getKey().toString().replace(":", ".");
        var s = template.replace("{amount}", String.valueOf(stack.getAmount()));

        if (!stack.getItemMeta().getDisplayName().isEmpty()) {
            item.customName(TextBuilder.buildComponent(s.replace(
                    "{id}",
                    ComponentSerializer.legacy(Objects.requireNonNull(stack.getItemMeta()
                                                                              .displayName()))
            )));
        } else {
            item.customName(TextBuilder.buildComponent(s.replace("{id}", id)));
        }

        item.setCustomNameVisible(true);
    }
}
