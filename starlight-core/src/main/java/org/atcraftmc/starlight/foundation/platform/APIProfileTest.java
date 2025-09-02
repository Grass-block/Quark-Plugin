package org.atcraftmc.starlight.foundation.platform;

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

    static boolean banner() {
        try {
            Class.forName("net.fabricmc.loader.api.FabricLoader");
            return true;
        } catch (Throwable e) {
            return false;
        }

    }

    static boolean youer() {
        try {
            Class.forName("net.neoforged.neoforge.common.NeoForgeMod");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    static boolean threadedRegion() {
        try {
            if (getVersionString().contains("Purpur")) {
                return false;
            }
            if (getVersionString().contains("purpur")) {
                return false;
            }
            if (getVersionString().contains("paper")) {
                return false;
            }
            Bukkit.getServer().getClass().getMethod("getRegionScheduler");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    static boolean adventurePlatform() {
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
        if (arclight()) {
            return APIProfile.ARCLIGHT;
        }
        if (banner()) {
            return APIProfile.BANNER;
        }
        if (youer()) {
            return APIProfile.YOUER;
        }
        if (threadedRegion()) {
            return APIProfile.FOLIA;
        }
        if (adventurePlatform()) {
            return APIProfile.PAPER;
        }
        if (spigot()) {
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

    static boolean isMixedServer() {
        return PLATFORM == APIProfile.BANNER || PLATFORM == APIProfile.YOUER || PLATFORM == APIProfile.ARCLIGHT;
    }
}
