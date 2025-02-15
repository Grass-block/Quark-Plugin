package org.atcraftmc.quark.display;

import me.gb2022.commons.Formating;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@QuarkModule(version = "1.1")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class AFK extends PackageModule {
    private final Listener actionListener = new PlayerActionListener();
    private final Map<String, Long> lastAFK = new HashMap<>();

    @Inject
    private LanguageEntry language;

    @Override
    public void enable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.restartAFKTimer(p);
        }
        BukkitUtil.registerEventListener(this.actionListener);
    }

    @Override
    public void disable() {
        this.lastAFK.clear();
        BukkitUtil.unregisterEventListener(this.actionListener);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        this.lastAFK.put(e.getPlayer().getName(), -1L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        this.lastAFK.put(e.getPlayer().getName(), -1L);
        this.restartAFKTimer(e.getPlayer());
    }

    private void broadcast(Player player, Consumer<Player> self, BiConsumer<Player, Player> others) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equals(player.getName())) {
                self.accept(player);
            } else {
                others.accept(p, player);
            }
        }
    }

    private void restartAFKTimer(Player player) {
        var id = player.getName();
        var tid = "quark:afk:delay@" + id;

        TaskService.async().cancel(tid);

        var timestamp = System.currentTimeMillis();

        TaskService.async().delay(tid, getConfig().getInt("timeout"), () -> {
            broadcast(
                    player,
                    (p) -> this.language.sendMessage(p, "left-self"),
                    (p, p2) -> this.language.sendMessage(p, "left", p2.getName())
            );
            this.lastAFK.put(id, timestamp);
        });

        if (!this.lastAFK.containsKey(player.getName())) {
            return;
        }
        if (this.lastAFK.get(player.getName()) == -1) {
            return;
        }

        var current = System.currentTimeMillis();
        var during = current - this.lastAFK.get(id);
        this.lastAFK.put(player.getName(), -1L);
        var time = Formating.formatDuringFull(during);

        broadcast(
                player,
                (p) -> this.language.sendMessage(p, "back-self", time),
                (p, p2) -> this.language.sendMessage(p, "back", p2.getName(), time)
        );
    }


    public class PlayerActionListener implements Listener {
        @EventHandler
        public void onPlayerChat(AsyncPlayerChatEvent e) {
            restartAFKTimer(e.getPlayer());
        }

        @EventHandler
        public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
            restartAFKTimer(e.getPlayer());
        }

        @EventHandler
        public void onBlockBreak(BlockBreakEvent e) {
            restartAFKTimer(e.getPlayer());
        }

        @EventHandler
        public void onBlockPlace(BlockPlaceEvent e) {
            restartAFKTimer(e.getPlayer());
        }

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent e) {
            restartAFKTimer(e.getPlayer());
        }

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent e) {
            restartAFKTimer(e.getPlayer());
        }

        @EventHandler
        public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e) {
            restartAFKTimer(e.getPlayer());
        }

        @EventHandler
        public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
            restartAFKTimer(e.getPlayer());
        }
    }
}
