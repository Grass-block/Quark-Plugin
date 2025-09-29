package org.atcraftmc.starlight.foundation.platform;

import me.gb2022.modular.PlatformProfile;

public enum APIProfile implements PlatformProfile {
    BUKKIT("bukkit"),
    SPIGOT("spigot"),
    PAPER("paper/airplane/purpur"),
    FOLIA("folia"),
    ARCLIGHT("forge/arclight/mohist"),
    BANNER("fabric/banner"),
    YOUER("NEOForge/youer");

    final String name;

    APIProfile(String s) {
        this.name = s;
    }

    public boolean isCompat(APIProfile platform) {
        return switch (this) {
            case BUKKIT -> platform == BUKKIT;
            case ARCLIGHT, SPIGOT, YOUER -> platform == BUKKIT || platform == SPIGOT;
            case PAPER -> platform != FOLIA;
            case FOLIA, BANNER -> true;
        };
    }


    @Override
    public String toString() {
        return this.name;
    }
}
