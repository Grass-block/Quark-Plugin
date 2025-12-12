package org.atcraftmc.starlight.music;

import org.atcraftmc.starlight.music.resolve.MusicData;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public interface PlayerUIRenderer {
    static String formatTime(long mss) {
        var fmt = new DecimalFormat("00");

        long minutes = (mss / (1000 * 60));
        long seconds = (mss % (1000 * 60)) / 1000;

        return fmt.format(minutes) + ":" + fmt.format(seconds);
    }

    void renderUI(Player player, MusicData currentMusic, int currentTick, boolean pause);
}
