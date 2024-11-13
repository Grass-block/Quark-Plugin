package org.atcraftmc.quark.display;

import me.gb2022.commons.Formating;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.*;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@QuarkModule
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class AFK extends PackageModule {
    private final Map<String, Long> lastAFK = new HashMap<>();

    @Inject
    private LanguageEntry language;


    @Override
    public void enable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.restartAFKTimer(p);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        this.lastAFK.remove(e.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        this.restartAFKTimer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.restartAFKTimer(event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        this.restartAFKTimer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        this.restartAFKTimer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        this.restartAFKTimer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e) {
        this.restartAFKTimer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        this.restartAFKTimer(e.getPlayer());
    }

    private void broadcast(Player player, Consumer<Player> action, Consumer<Player> excluded) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equals(player.getName())) {
                action.accept(p);
            } else {
                excluded.accept(p);
            }
        }
    }

    private void restartAFKTimer(Player player) {
        var id = player.getName();
        var tid = "quark:afk:delay@" + id;

        TaskService.async().cancel(tid);
        TaskService.async().delay(tid, getConfig().getInt("timeout"), () -> {
            broadcast(player, (p) -> this.language.sendMessage(p, "left-self"), (p) -> this.language.sendMessage(p, "left", p.getName()));
            this.lastAFK.put(id, System.currentTimeMillis());
        });
        if (!this.lastAFK.containsKey(player.getName())) {
            return;
        }
        if (this.lastAFK.get(player.getName()) == -1) {
            return;
        }

        var current = System.currentTimeMillis();
        var during = current - lastAFK.get(id);
        this.lastAFK.put(player.getName(), -1L);
        var time = Formating.formatDuringFull(during);

        broadcast(
                player,
                (p) -> this.language.sendMessage(p, "back-self", time),
                (p) -> this.language.sendMessage(p, "back", p.getName(), time)
        );
    }
}
