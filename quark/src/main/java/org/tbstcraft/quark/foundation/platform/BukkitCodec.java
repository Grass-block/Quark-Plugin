package org.tbstcraft.quark.foundation.platform;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Objects;

public interface BukkitCodec {
    static Location fromNBT(NBTTagCompound tag) {

        String wid = tag.getString("world");

        return new Location(
                Bukkit.getWorld(wid),
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getDouble("z"),
                tag.hasKey("yaw") ? tag.getFloat("yaw") : 0.0f,
                tag.hasKey("pitch") ? tag.getFloat("pitch") : 0.0f
        );
    }

    static NBTTagCompound toNBT(Location loc) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("world", Objects.requireNonNull(loc.getWorld()).getName());
        tag.setDouble("x", loc.getX());
        tag.setDouble("y", loc.getY());
        tag.setDouble("z", loc.getZ());
        tag.setFloat("yaw", loc.getYaw());
        tag.setFloat("pitch", loc.getPitch());
        return tag;
    }

    static String toString(Location loc) {
        return "[%s: %f, %f, %f - %f, %f]".formatted(
                Objects.requireNonNull(loc.getWorld()).getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        );
    }
}
