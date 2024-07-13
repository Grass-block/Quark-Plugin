package org.tbstcraft.quark.utilities.viewdistance;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.data.PlayerDataService;

public interface ViewDistanceStrategy {
    int determine(Player player, int currentValue);

    class CustomSetting implements ViewDistanceStrategy {
        public static boolean has(Player player) {
            return get(player) != -1;
        }

        public static void set(Player player, int value) {
            NBTTagCompound tag = PlayerDataService.getEntry(player.getName(), "view-distance");
            tag.setByte("custom", (byte) value);
            PlayerDataService.save(player.getName());
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
    }
}
