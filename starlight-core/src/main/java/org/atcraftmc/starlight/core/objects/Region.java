package org.atcraftmc.starlight.core.objects;

import org.bson.BsonDocument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.joml.Vector3d;

import java.util.UUID;

/**
 * separated xyz coordinate is ONLY for acceleration.
 */
public final class Region {
    private final UUID uuid;
    private final BsonDocument extraMetadata;
    private final String world;
    private final Vector3d point0;
    private final Vector3d point1;

    private double x0;
    private double y0;
    private double z0;
    private double x1;
    private double y1;
    private double z1;
    private String name;

    /**
     * We don't care the position since we sort them correctly
     *
     * @param world world
     * @param p0    point1
     * @param p1    point2
     */
    public Region(String name, World world, Location p0, Location p1) {
        this.world = world.getName();
        this.point0 = new Vector3d(p0.getX(), p0.getY(), p0.getZ());
        this.point1 = new Vector3d(p1.getX(), p1.getY(), p1.getZ());
        this.renderXYZ(p0, p1);

        this.uuid = UUID.randomUUID();
        this.extraMetadata = new BsonDocument();
        this.name = name;
    }

    public Region(String name, String world, Vector3d p0, Vector3d p1) {
        this.world = world;
        this.point0 = p0;
        this.point1 = p1;
        this.renderXYZ(p0, p1);

        this.uuid = UUID.randomUUID();
        this.extraMetadata = new BsonDocument();
        this.name = name;
    }

    public Region(UUID uuid, String name, String world, Vector3d p0, Vector3d p1, BsonDocument extraMetadata) {
        this.world = world;
        this.point0 = p0;
        this.point1 = p1;
        this.renderXYZ(p0, p1);
        this.extraMetadata = extraMetadata;
        this.uuid = uuid;
        this.name = name;
    }

    public void renderXYZ(Location p0, Location p1) {
        var xx0 = p0.getX();
        var yy0 = p0.getY();
        var zz0 = p0.getZ();
        var xx1 = p1.getX();
        var yy1 = p1.getY();
        var zz1 = p1.getZ();

        resize(xx0, yy0, zz0, xx1, yy1, zz1);
    }

    public void renderXYZ(Vector3d p0, Vector3d p1) {
        var xx0 = p0.x();
        var yy0 = p0.y();
        var zz0 = p0.z();
        var xx1 = p1.x();
        var yy1 = p1.y();
        var zz1 = p1.z();

        resize(xx0, yy0, zz0, xx1, yy1, zz1);
    }

    private void resize(double xx0, double yy0, double zz0, double xx1, double yy1, double zz1) {
        this.x0 = Math.min(xx0, xx1);
        this.y0 = Math.min(yy0, yy1);
        this.z0 = Math.min(zz0, zz1);
        this.x1 = Math.max(xx0, xx1);
        this.y1 = Math.max(yy0, yy1);
        this.z1 = Math.max(zz0, zz1);
    }

    public World getWorld() {
        return Bukkit.getWorld(this.world);
    }

    public Location getPoint0() {
        return new Location(getWorld(), this.point0.x(), this.point0.y(), this.point0.z());
    }

    public Location getPoint1() {
        return new Location(getWorld(), this.point1.x(), this.point1.y(), this.point1.z());
    }

    public void setPoint0(Location point0) {
        this.point0.set(point0.getX(), point0.getY(), point0.getZ());
        this.renderXYZ(this.point0, this.point1);
    }

    public void setPoint1(Location point1) {
        this.point1.set(point1.getX(), point1.getY(), point1.getZ());
        this.renderXYZ(this.point0, this.point1);
    }

    public Location getMinPoint() {
        return new Location(getWorld(), this.x0, this.y0, this.z0);
    }

    public Location getMaxPoint() {
        return new Location(getWorld(), this.x1, this.y1, this.z1);
    }

    public BsonDocument getExtraMetadata() {
        return extraMetadata;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("Region{");
        sb.append("world=").append(this.world);
        sb.append(", x0=").append(this.x0);
        sb.append(", y0=").append(this.y0);
        sb.append(", z0=").append(this.z0);
        sb.append(", x1=").append(this.x1);
        sb.append(", y1=").append(this.y1);
        sb.append(", z1=").append(this.z1);
        sb.append('}');
        return sb.toString();
    }

    public String getWorldId() {
        return this.world;
    }
}
