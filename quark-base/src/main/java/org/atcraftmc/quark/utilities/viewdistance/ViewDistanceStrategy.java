package org.atcraftmc.quark.utilities.viewdistance;

import org.bukkit.entity.Player;

public interface ViewDistanceStrategy {
    /**
     * @param player       target player
     * @param currentValue origin view distance
     * @return calculated view distance
     */
    int determine(Player player, int currentValue);

    default boolean remindPlayer(Player player, boolean originalRemind) {
        return false;
    }
}
