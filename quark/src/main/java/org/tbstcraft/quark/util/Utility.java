package org.tbstcraft.quark.util;

public interface Utility {
    static String formatDuring(long mss) {
        long hours = mss / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        return hours + "h " + minutes + "m "
                + seconds + "s ";
    }

    static String formatDuringShort(long mss) {
        long minutes = (mss / (1000 * 60));
        long seconds = (mss % (1000 * 60)) / 1000;
        return minutes + ":"
                + seconds;
    }
}
