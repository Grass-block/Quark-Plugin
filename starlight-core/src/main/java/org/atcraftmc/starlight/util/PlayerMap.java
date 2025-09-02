package org.atcraftmc.starlight.util;

import org.bukkit.entity.Player;
import org.atcraftmc.starlight.internal.PlayerIdentificationService;

import java.util.HashMap;
import java.util.function.Function;

public final class PlayerMap<V> extends HashMap<String, V> {
    public void put(Player player, V value) {
        put(PlayerIdentificationService.transformPlayer(player), value);
    }

    public V get(Player player) {
        return get(PlayerIdentificationService.transformPlayer(player));
    }

    public boolean contains(Player player) {
        return this.containsKey(PlayerIdentificationService.transformPlayer(player));
    }

    public V remove(Player player) {
        return remove(PlayerIdentificationService.transformPlayer(player));
    }

    public V computeIfAbsent(Player player, Function<Player, ? extends V> gf) {
        return computeIfAbsent(PlayerIdentificationService.transformPlayer(player), (k) -> gf.apply(player));
    }
}
