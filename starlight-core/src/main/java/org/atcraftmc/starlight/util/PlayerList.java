package org.atcraftmc.starlight.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class PlayerList {
    private final Set<UUID> players = new HashSet<>();

    public void add(final Player player) {
        this.players.add(player.getUniqueId());
    }

    public void remove(final Player player) {
        this.players.remove(player.getUniqueId());
    }

    public void foreach(Consumer<Player> action) {
        for (UUID uuid : this.players) {
            action.accept(Bukkit.getPlayer(uuid));
        }
    }

    public Set<UUID> getPlayers() {
        return this.players;
    }

    public Set<Player> getPlayerObjects() {
        return this.players.stream().map(Bukkit::getPlayer).collect(Collectors.toSet());
    }
}
