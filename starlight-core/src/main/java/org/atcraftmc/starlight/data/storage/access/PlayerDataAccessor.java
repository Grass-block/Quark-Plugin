package org.atcraftmc.starlight.data.storage.access;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.internal.PlayerIdentificationService;

import java.util.HashMap;
import java.util.Map;

public final class PlayerDataAccessor<I> implements Listener {
    private final PlayerDataAccess<I> dataAccess;
    private final I defaultValue;
    private final Map<String, I> values = new HashMap<>();

    public PlayerDataAccessor(I defaultValue, PlayerDataAccess<I> dataAccess) {
        this.dataAccess = dataAccess;
        this.defaultValue = defaultValue;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        load(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        remove(event.getPlayer());
    }


    public void remove(Player player) {
        this.dataAccess.setAndSave(player, get(player));
        this.values.remove(PlayerIdentificationService.transformPlayer(player));
    }

    public void load(Player player) {
        try {
            this.values.put(PlayerIdentificationService.transformPlayer(player), this.dataAccess.get(player));
        } catch (Exception e) {
            this.values.put(PlayerIdentificationService.transformPlayer(player), this.defaultValue);
        }
    }

    public I get(Player player) {
        return this.values.computeIfAbsent(PlayerIdentificationService.transformPlayer(player),(k)->this.defaultValue);
    }

    public void set(Player player, I value) {
        this.values.put(PlayerIdentificationService.transformPlayer(player), value);
    }


    public void init() {
        BukkitUtil.registerEventListener(this);

        for (var player : Bukkit.getOnlinePlayers()) {
            load(player);
        }
    }

    public void destroy() {
        BukkitUtil.unregisterEventListener(this);

        for (var player : Bukkit.getOnlinePlayers()) {
            this.remove(player);
        }
    }
}
