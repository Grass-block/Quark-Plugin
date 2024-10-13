package org.atcraftmc.quark.security;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.tool.SelectionWand;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.event.platform.BlockInteractEvent;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.event.platform.PlayerInputEvent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import org.atcraftmc.quark.security.event.WEAction;
import org.atcraftmc.quark.security.event.WESessionEditEvent;
import org.atcraftmc.quark.security.event.WESessionPreEditEvent;
import org.atcraftmc.quark.security.event.WESessionSelectEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.tbstcraft.quark.data.config.ConfigEntry;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.foundation.region.SimpleRegion;
import org.tbstcraft.quark.framework.service.*;

import java.util.HashMap;
import java.util.Map;

@QuarkService(id = "we-session-track")
public interface WESessionTrackService extends Service {
    @ServiceInject
    ServiceHolder<WESessionTrackService> INSTANCE = new ServiceHolder<>();

    static SimpleRegion getRegion(Player p) {
        return INSTANCE.get().getPlayerRegion(p);
    }

    static SimpleRegion getRegionNatively(Player p) {
        return INSTANCE.get().getPlayerRegionNatively(p);
    }

    static WEAction getLatestAction(Player player) {
        return INSTANCE.get().getPlayerLatestAction(player);
    }


    @ServiceProvider
    static WESessionTrackService create(ConfigEntry cfg) {
        return new ServiceImplementation();
    }

    SimpleRegion getPlayerRegion(Player p);

    SimpleRegion getPlayerRegionNatively(Player p);

    WEAction getPlayerLatestAction(Player p);

    final class ServiceImplementation implements WESessionTrackService, Listener {
        private final Map<Player, SimpleRegion> regions = new HashMap<>();
        private final Map<Player, WEAction> latestActions = new HashMap<>();

        //lifetime
        @Override
        public void onEnable() {
            WorldEdit.getInstance().getEventBus().register(this);
            BukkitUtil.registerEventListener(this);
        }

        @Override
        public void onDisable() {
            WorldEdit.getInstance().getEventBus().unregister(this);
            BukkitUtil.unregisterEventListener(this);
        }

        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requirePlugin("WorldEdit");
            Compatibility.requireClass(()->Class.forName("com.sk89q.worldedit.util.HandSide"));
        }

        //api impl
        @Override
        public SimpleRegion getPlayerRegion(Player p) {
            if (!this.regions.containsKey(p) || this.regions.get(p) == null) {
                this.regions.put(p, getPlayerRegionNatively(p));
            }

            return regions.get(p);
        }

        @Override
        public WEAction getPlayerLatestAction(Player p) {
            return this.latestActions.get(p);
        }

        @Override
        public SimpleRegion getPlayerRegionNatively(Player p) {
            var player = BukkitAdapter.adapt(p);
            var world = BukkitAdapter.adapt(p.getWorld());

            try {
                var r = WorldEdit.getInstance().getSessionManager().get(player).getSelection(world);

                var x0 = r.getMinimumPoint().getBlockX();
                var y0 = r.getMinimumPoint().getBlockY();
                var z0 = r.getMinimumPoint().getBlockZ();
                var x1 = r.getMaximumPoint().getBlockX();
                var y1 = r.getMaximumPoint().getBlockY();
                var z1 = r.getMaximumPoint().getBlockZ();

                return new SimpleRegion(p.getWorld(), x0, y0, z0, x1, y1, z1);

            } catch (IncompleteRegionException e) {
                return null;
            }
        }


        //WorldEdit event handle
        @Subscribe(priority = com.sk89q.worldedit.util.eventbus.EventHandler.Priority.VERY_LATE)
        public void handleInput(PlayerInputEvent event) {
            var p=event.getPlayer();
            var session = WorldEdit.getInstance().getSessionManager().get(p);
            if (!(session.getTool(p.getItemInHand(HandSide.MAIN_HAND).getType()) instanceof SelectionWand)) {
                return;
            }

            event.setCancelled(true);
        }

        @Subscribe(priority = com.sk89q.worldedit.util.eventbus.EventHandler.Priority.VERY_LATE)
        public void handleBlockInput(BlockInteractEvent event) {
            if (!(event.getCause() instanceof com.sk89q.worldedit.entity.Player p)) {
                return;
            }

            handleWEEvent(p);
        }

        @Subscribe(priority = com.sk89q.worldedit.util.eventbus.EventHandler.Priority.VERY_LATE)
        public void handleCommand(CommandEvent event) {
            if (!(event.getActor() instanceof com.sk89q.worldedit.entity.Player p)) {
                return;
            }

            var b1 = event.getArguments().startsWith("//pos1");
            var b2 = event.getArguments().startsWith("//pos2");
            var b11 = event.getArguments().startsWith("//hpos1");
            var b12 = event.getArguments().startsWith("//hpos2");

            if (b1 || b2 || b11 || b12) {
                handleWEEvent(p);
            }
        }

        @Subscribe(priority = com.sk89q.worldedit.util.eventbus.EventHandler.Priority.VERY_EARLY)
        public void onSessionEdit(EditSessionEvent event) {
            if (event.getActor() == null) {
                return;
            }

            var player = Bukkit.getPlayer(event.getActor().getUniqueId());

            if (player == null) {
                return;
            }

            var e1 = new WESessionPreEditEvent(player, getPlayerRegion(player), event.getStage());
            Bukkit.getPluginManager().callEvent(e1);
            if (e1.isCancelled()) {
                event.setExtent(new NullExtent());
                return;
            }

            var evt = new WESessionEditEvent(player, event.getExtent());
            Bukkit.getPluginManager().callEvent(evt);
            event.setExtent(evt.getMask());
        }

        private void handleWEEvent(com.sk89q.worldedit.entity.Player p) {
            var session = WorldEdit.getInstance().getSessionManager().get(p);
            if (!(session.getTool(p.getItemInHand(HandSide.MAIN_HAND).getType()) instanceof SelectionWand)) {
                return;
            }

            var player1 = BukkitAdapter.adapt(p);
            var session1 = WorldEdit.getInstance().getSessionManager().get(p);
            var world = p.getWorld();
            try {
                var region = session1.getSelection(world);
                var p1 = BukkitAdapter.adapt(player1.getWorld(), region.getBoundingBox().getMinimumPoint());
                var p2 = BukkitAdapter.adapt(player1.getWorld(), region.getBoundingBox().getMaximumPoint());
                var r = new SimpleRegion(p1, p2);

                this.regions.put(player1, r);
            } catch (IncompleteRegionException ignored) {
            }

            var player = Bukkit.getPlayer(p.getUniqueId());
            var evt = new WESessionSelectEvent(player, getPlayerRegion(player));

            Bukkit.getPluginManager().callEvent(evt);
        }

        @EventHandler
        public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
            if (e.getMessage().startsWith("//stack") || e.getMessage().startsWith("/worldedit:/stack")) {
                this.latestActions.put(e.getPlayer(), WEAction.STACK);
                return;
            }
            if (e.getMessage().startsWith("//paste") || e.getMessage().startsWith("/worldedit:/paste")) {
                this.latestActions.put(e.getPlayer(), WEAction.PASTE);
                return;
            }
            if (e.getMessage().startsWith("//set") || e.getMessage().startsWith("/worldedit:/set")) {
                this.latestActions.put(e.getPlayer(), WEAction.FILL);
                return;
            }
            this.latestActions.put(e.getPlayer(), WEAction.OTHER);
        }
    }
}
