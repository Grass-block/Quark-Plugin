package org.atcraftmc.starlight.utilities.viewdistance;

import me.gb2022.commons.math.MathHelper;
import org.atcraftmc.starlight.core.data.flex.TableColumn;
import org.atcraftmc.starlight.data.PlayerDataService;
import org.bukkit.entity.Player;

public final class CustomSettingStrategy implements ViewDistanceStrategy {
    public static final TableColumn<Integer> CUSTOM = TableColumn.integer("view_distance", -1);

    public static boolean has(Player player) {
        return CUSTOM.get(PlayerDataService.PLAYER_LOCAL, player.getUniqueId()) != -1;
    }

    public static int set(Player player, int value) {
        value = (int) MathHelper.clamp(value, 2, 32);

        CUSTOM.set(PlayerDataService.PLAYER_LOCAL, player.getUniqueId(), value);

        return value;
    }

    public static void clear(Player player) {
        set(player, -1);
    }

    public static int get(Player player) {
        if (!has(player)) {
            return -1;
        }

        return CUSTOM.get(PlayerDataService.PLAYER_LOCAL, player.getUniqueId());
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
