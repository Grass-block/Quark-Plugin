package org.atcraftmc.quark.utilities.viewdistance;

import me.gb2022.commons.math.MathHelper;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.data.storage.access.PlayerDataAccess;

public final class CustomSettingStrategy implements ViewDistanceStrategy {
    public static final PlayerDataAccess<Byte> STORAGE_ACCESS = PlayerDataAccess.byteElement("view_distance_custom");

    public static boolean has(Player player) {
        return get(player) != -1;
    }

    public static int set(Player player, int value) {
        value = (int) MathHelper.clamp(value, 2, 32);

        STORAGE_ACCESS.setAndSave(player, (byte) value);

        return value;
    }

    public static void clear(Player player) {
        set(player, -1);
    }

    public static int get(Player player) {
        if (!STORAGE_ACCESS.contains(player)) {
            return -1;
        }

        return STORAGE_ACCESS.get(player);
    }

    @Override
    public int determine(Player player, int currentValue) {
        if (has(player)) {
            return get(player);
        }
        return currentValue;
    }

    @Override
    public boolean remindPlayer(Player player, boolean originalRemind) {
        return true;
    }
}
