package org.tbstcraft.quark.util.api;

public enum APIProfile {
    BUKKIT("bukkit"),
    SPIGOT("spigot"),
    PAPER("paper/airplane/purpur"),
    FOLIA("folia"),
    ARCLIGHT("arclight/mohist");

    final String name;

    APIProfile(String s) {
        this.name = s;
    }

    public boolean isCompat(APIProfile platform) {
        return switch (this) {
            case BUKKIT -> platform == BUKKIT;
            case ARCLIGHT, SPIGOT -> platform == BUKKIT || platform == SPIGOT;
            case PAPER -> platform != FOLIA;
            case FOLIA -> true;
        };
    }


    @Override
    public String toString() {
        return this.name;
    }
}
