package org.atcraftmc.quark.utilities.viewdistance;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.data.PlayerDataService;

public final class CustomSettingStrategy implements ViewDistanceStrategy {
    public static boolean has(Player player) {
        return get(player) != -1;
    }

    public static int set(Player player, int value) {
        NBTTagCompound tag = PlayerDataService.getEntry(player.getName(), "view-distance");
        tag.setByte("custom", (byte) value);
        PlayerDataService.save(player.getName());
        return value;
    }

    public static void clear(Player player) {
        set(player, -1);
    }

    public static int get(Player player) {
        NBTTagCompound tag = PlayerDataService.getEntry(player.getName(), "view-distance");

        if (!tag.hasKey("custom")) {
            return -1;
        }

        return tag.getByte("custom");
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
