package org.tbstcraft.quark.utilities.viewdistance;

import org.bukkit.entity.Player;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;

public final class NetworkPingStrategy implements ViewDistanceStrategy {

    @Override
    public int determine(Player player, int currentValue) {
        int ping = Integer.parseInt(PlaceHolderService.PLAYER.get("ping-value", player));
        long playtime = System.currentTimeMillis() - player.getLastLogin();
        if (playtime < 6000) {
            return (int) (currentValue * 0.3);
        }
        if (playtime < 12000) {
            return (int) (currentValue * 0.6);
        }

        if (ping == 0) {
            return currentValue;
        }
        if (ping < 20) {
            return currentValue + 2;
        }
        if (ping < 40) {
            return currentValue + 4;
        }
        if (ping < 120) {
            return currentValue - 2;
        }
        if (ping < 180) {
            return currentValue - 4;
        }
        return currentValue / 2;
    }
}
