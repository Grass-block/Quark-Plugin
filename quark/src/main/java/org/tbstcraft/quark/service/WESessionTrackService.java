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
import org.tbstcraft.quark.event.WorldeditSectionUpdateEvent;
import org.tbstcraft.quark.util.api.BukkitUtil;
import org.tbstcraft.quark.util.Region;
import org.tbstcraft.quark.util.ObjectContainer;

import java.util.HashMap;
import java.util.Objects;

public interface WESessionTrackService extends Service {
    ObjectContainer<WESessionTrackService> INSTANCE = new ObjectContainer<>();

    static void init() {
        INSTANCE.set(create());
        INSTANCE.get().onEnable();
    }

    static void stop() {
        INSTANCE.get().onDisable();
    }

    static void updateP0(Player player, Location loc) {
        INSTANCE.get().updatePoint0(player, loc);
    }

    static void updateP1(Player player, Location loc) {
        INSTANCE.get().updatePoint1(player, loc);
    }

    static Region getRegion(Player p) {
        return INSTANCE.get().getPlayerRegion(p);
    }

    static void update(Player p) {
        INSTANCE.get().updateRegion(p);
    }

    static WESessionTrackService create() {
        return new ServiceImplementation();
    }

    void updatePoint0(Player player, Location loc);

    void updatePoint1(Player player, Location loc);

    Region getPlayerRegion(Player p);

    void updateRegion(Player p);


    final class ServiceImplementation implements WESessionTrackService, Listener {
        private final HashMap<String, Region> sessions = new HashMap<>();

        @Override
        public void onEnable() {
            BukkitUtil.registerEventListener(this);
        }

        @Override
        public void onDisable() {
            BukkitUtil.unregisterEventListener(this);
        }

        @Override
        public void updatePoint0(Player player, Location loc) {
            Region r = getRegion(player);
            r.setPoint0(loc);
        }

        @Override
        public void updatePoint1(Player player, Location loc) {
            Region r = getRegion(player);
            r.setPoint1(loc);
        }

        @Override
        public Region getPlayerRegion(Player p) {
            if (sessions.containsKey(p.getName())) {
                return sessions.get(p.getName());
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
            sessions.put(p.getName(), r);
            return r;
        }

        @Override
        public void updateRegion(Player p) {
            Region r = sessions.get(p.getName());
            if (r == null) {
                return;
            }
            if (!r.isComplete()) {
                return;
            }
            Bukkit.getPluginManager().callEvent(new WorldeditSectionUpdateEvent(p, r));
        }

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
