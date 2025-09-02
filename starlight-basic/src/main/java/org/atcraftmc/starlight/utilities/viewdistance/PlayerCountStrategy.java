package org.atcraftmc.starlight.utilities.viewdistance;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlayerCountStrategy implements ViewDistanceStrategy {

    @Override
    public int determine(Player player, int currentValue) {
        int players = Bukkit.getOnlinePlayers().size();

        if (players < 4) {
            return currentValue;
        } else if (players < 8) {
            return Math.max(currentValue - 4, 8);
        } else if (players < 13) {
            return Math.max(currentValue - 8, 6);
        }
        return 4;
    }
}
