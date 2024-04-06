package org.tbstcraft.quark.util.api;

import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;

import java.util.Objects;

public interface APIProfileTest {
    APIProfile PLATFORM = APIProfileTest.test();

    static boolean arclight() {
        try {
            Class.forName("net.minecraftforge.fml.common.Mod");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    static boolean folia() {
        try {
            if (getVersionString().contains("Purpur")) {
                return false;
            }
            if (getVersionString().contains("purpur")) {
                return false;
            }
            if(getVersionString().contains("paper")){
                return false;
            }
            Bukkit.getServer().getClass().getMethod("getRegionScheduler");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    static boolean paper() {
        try {
            Bukkit.getConsoleSender().getClass().getMethod("sendMessage", ComponentLike.class);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    static boolean spigot() {
        try {
            Bukkit.getConsoleSender().getClass().getMethod("spigot");
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    static APIProfile test() {
        if (APIProfileTest.arclight()) {
            return APIProfile.ARCLIGHT;
        }
        if (APIProfileTest.folia()) {
            return APIProfile.FOLIA;
        }
        if (APIProfileTest.paper()) {
            return APIProfile.PAPER;
        }
        if (APIProfileTest.spigot()) {
            return APIProfile.SPIGOT;
        }

        return APIProfile.BUKKIT;
    }

    static APIProfile getAPIProfile() {
        return PLATFORM;
    }


    static boolean isFoliaServer() {
        return Objects.equals(getAPIProfile(), APIProfile.FOLIA);
    }

    static boolean isPaperServer() {
        return Objects.equals(getAPIProfile(), APIProfile.PAPER);
    }

    static boolean isSpigotServer() {
        return Objects.equals(getAPIProfile(), APIProfile.SPIGOT);
    }

    static boolean isSpigotCompat() {
        return !Objects.equals(getAPIProfile(), APIProfile.BUKKIT);
    }

    static boolean compatWith(APIProfile platform) {
        return getAPIProfile().isCompat(platform);
    }

    static boolean isPaperCompat() {
        return isFoliaServer() || isPaperServer();
    }

    static String getVersionString() {
        return Bukkit.getVersionMessage().toLowerCase();
    }

    static boolean isArclightBasedServer() {
        return PLATFORM == APIProfile.ARCLIGHT;
    }
}
