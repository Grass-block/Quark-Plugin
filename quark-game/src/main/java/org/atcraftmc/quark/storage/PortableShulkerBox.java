package org.atcraftmc.quark.storage;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.TaskService;

import java.util.HashMap;
import java.util.Map;

@SLModule(version = "1.2")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class PortableShulkerBox extends PackageModule {
    private final Map<String, Session> sessions = new HashMap<>();

    @Override
    public void disable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.close(p.getName());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        PlayerInventory inv = event.getPlayer().getInventory();

        if (!inv.getItemInMainHand().getType().getKey().getKey().endsWith("shulker_box")) {
            return;
        }

        TaskService.entity(event.getPlayer()).run(() -> {
            Session session = new Session(inv.getItemInMainHand(), event.getPlayer());
            this.sessions.put(event.getPlayer().getName(), session);
        });
    }

    public void close(String player) {
        if (!this.sessions.containsKey(player)) {
            return;
        }
        TaskService.entity(Bukkit.getPlayerExact(player)).run(() -> {
            this.sessions.get(player).close();
            this.sessions.remove(player);
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        this.close(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.close(event.getPlayer().getName());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (this.sessions.containsKey(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }


    static final class Session {
        private final ItemStack item;
        private final BlockStateMeta meta;
        private final ShulkerBox state;

        public Session(ItemStack item, Player target) {
            this.item = item;
            this.meta = ((BlockStateMeta) item.getItemMeta());
            this.state = (ShulkerBox) meta.getBlockState();
            Inventory inventory = this.state.getInventory();
            target.openInventory(inventory);
        }

        public void close() {
            this.meta.setBlockState(state);
            this.item.setItemMeta(this.meta);
        }
    }
}
