package org.atcraftmc.quark_velocity.util;

import com.velocitypowered.api.proxy.Player;

public interface VelocityUtil {
    static boolean isSameServer(Player p1, Player p2) {
        var c1 = p1.getCurrentServer();
        var c2 = p2.getCurrentServer();

        if (c1.isEmpty() && c2.isEmpty()) {
            return true;
        }

        if (c1.isEmpty()) {
            return false;
        }

        if (c2.isEmpty()) {
            return false;
        }

        return c1.get().getServerInfo().equals(c2.get().getServerInfo());
    }
}
