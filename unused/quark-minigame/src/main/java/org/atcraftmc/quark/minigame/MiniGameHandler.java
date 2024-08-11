package org.atcraftmc.quark.minigame;

import org.bukkit.entity.Player;

public interface MiniGameHandler {

    void addPlayer(Player player, Game game);

    default void onGameStart(Game game) {

    }
}
