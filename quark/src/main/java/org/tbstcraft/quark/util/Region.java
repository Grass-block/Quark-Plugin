package org.tbstcraft.quark.util;

import me.gb2022.commons.math.AABB;
import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class Region {
    private Location point0;
    private Location point1;
    private World world;


    public Region(World world, int x0, int y0, int z0, int x1, int y1, int z1) {
        this.world = world;
        this.point0 = new Location(world, x0, y0, z0);
        this.point1 = new Location(world, x1, y1, z1);
    }

    public Region(NBTTagCompound tag) {
        this(
                Bukkit.getWorld(tag.getString("world")),
                tag.getInteger("x0"),
                tag.getInteger("y0"),
                tag.getInteger("z0"),
                tag.getInteger("x1"),
                tag.getInteger("y1"),
                tag.getInteger("z1")
        );
    }

    public Location getPoint0() {
        return point0;
    }

    public void setPoint0(Location location) {
        point0 = location;
    }

    public Location getPoint1() {
        return point1;
    }

    public void setPoint1(Location location) {
        point1 = location;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isComplete() {
        return point0 != null && point1 != null;
    }

    public boolean inBound(Location location) {
        if (!isComplete()) {
            return false;
        }

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        double minX = Math.min(point0.getX(), point1.getX());
        double minY = Math.min(point0.getY(), point1.getY());
        double minZ = Math.min(point0.getZ(), point1.getZ());
        double maxX = Math.max(point0.getX(), point1.getX());
        double maxY = Math.max(point0.getY(), point1.getY());
        double maxZ = Math.max(point0.getZ(), point1.getZ());

        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public void clear() {
        point0 = null;
        point1 = null;
    }

    public NBTTagCompound serialize() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("world", this.world.getName());

        int minX = (int) Math.min(point0.getX(), point1.getX());
        int minY = (int) Math.min(point0.getY(), point1.getY());
        int minZ = (int) Math.min(point0.getZ(), point1.getZ());
        int maxX = (int) Math.max(point0.getX(), point1.getX());
        int maxY = (int) Math.max(point0.getY(), point1.getY());
        int maxZ = (int) Math.max(point0.getZ(), point1.getZ());

        tag.setInteger("x0", minX);
        tag.setInteger("y0", minY);
        tag.setInteger("z0", minZ);
        tag.setInteger("x1", maxX);
        tag.setInteger("y1", maxY);
        tag.setInteger("z1", maxZ);
        return tag;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public String toString() {
        return "[%d,%d,%d] -> [%d,%d,%d] @%s".formatted(
                (int) Math.min(point0.getX(), point1.getX()),
                (int) Math.min(point0.getY(), point1.getY()),
                (int) Math.min(point0.getZ(), point1.getZ()),
                (int) Math.max(point0.getX(), point1.getX()),
                (int) Math.max(point0.getY(), point1.getY()),
                (int) Math.max(point0.getZ(), point1.getZ()),
                this.world.getName()
        );
    }

    public AABB asAABB() {
        return new AABB(
                this.getPoint0().getX(),
                this.getPoint0().getY(),
                this.getPoint0().getZ(),
                this.getPoint1().getX(),
                this.getPoint1().getY(),
                this.getPoint1().getZ()
        );
    }

    public boolean contains(Location location) {
        if (location.getWorld() != this.getPoint0().getWorld()) {
            return false;
        }
        return this.asAABB().isVectorInside(location.getX(), location.y(), location.getZ());
    }
}
