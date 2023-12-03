package org.tbstcraft.quark.service;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.event.WorldeditSectionUpdateEvent;
import org.tbstcraft.quark.util.Region;

import java.util.HashMap;
import java.util.Objects;

public interface WorldEditLocalSessionTracker {
    HashMap<String, Region> LOCALIZED_SESSIONS = new HashMap<>();
    EventHolder EVENT_HOLDER = new EventHolder();

    static void init() {
        Bukkit.getPluginManager().registerEvents(EVENT_HOLDER, Quark.PLUGIN);
    }

    static void stop() {
        PlayerCommandPreprocessEvent.getHandlerList().unregister(EVENT_HOLDER);
        PlayerInteractEvent.getHandlerList().unregister(EVENT_HOLDER);
    }

    static void updateP0(Player player, Location loc) {
        Region r = getRegion(player);
        r.setPoint0(loc);
    }

    static void updateP1(Player player, Location loc) {
        Region r = getRegion(player);
        r.setPoint1(loc);
    }

    static Region getRegion(Player p) {
        if (LOCALIZED_SESSIONS.containsKey(p.getName())) {
            return LOCALIZED_SESSIONS.get(p.getName());
        }
        Region r = new Region(
                p.getWorld(),
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE
        );
        LOCALIZED_SESSIONS.put(p.getName(), r);
        return r;
    }

    static void update(Player p) {
        Region r = LOCALIZED_SESSIONS.get(p.getName());
        if (r == null) {
            return;
        }
        if (!r.isComplete()) {
            return;
        }
        Bukkit.getPluginManager().callEvent(new WorldeditSectionUpdateEvent(p, r));
    }

    final class EventHolder implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onInteractEvent(PlayerInteractEvent event) {
            ItemStack i = event.getPlayer().getInventory().getItem(event.getPlayer().getInventory().getHeldItemSlot());
            if (!(Bukkit.getPluginManager().isPluginEnabled("WorldEdit")
                    && i != null
                    && i.getType() == Material.WOODEN_AXE
                    && event.getPlayer().hasPermission("worldedit.region.*")
            )) {
                return;
            }
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                updateP0(event.getPlayer(), Objects.requireNonNull(event.getClickedBlock()).getLocation());
            }
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                updateP1(event.getPlayer(), Objects.requireNonNull(event.getClickedBlock()).getLocation());
            }
            update(event.getPlayer());
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onCommandEvent(PlayerCommandPreprocessEvent event) {
            if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
                return;
            }
            if (event.getMessage().startsWith("//pos1")) {
                updateP0(event.getPlayer(), event.getPlayer().getEyeLocation());
                update(event.getPlayer());
                return;
            }
            if (event.getMessage().startsWith("//pos2")) {
                updateP1(event.getPlayer(), event.getPlayer().getEyeLocation());
                update(event.getPlayer());
            }
        }
    }
}
