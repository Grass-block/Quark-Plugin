package org.atcraftmc.starlight.utilities.font;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;

public final class ParticleFontComponent {
    private final Font font;
    private final float size;
    private final double density;
    private final World world;
    private final double baseX;
    private final double baseY;
    private final double baseZ;
    private final float rotX;
    private final float rotY;
    private final float rotZ;
    private final String text;
    private Quaternionf quaternion;
    private int count = 0;

    public ParticleFontComponent(Font font, float size, double density, Location location, float zRot, String text) {
        this.font = font;
        this.size = size;
        this.density = density;
        this.world = location.getWorld();
        this.baseX = location.getX();
        this.baseY = location.getY();
        this.baseZ = location.getZ();
        this.rotX = location.getYaw();
        this.rotY = location.getPitch();
        this.rotZ = zRot;
        this.text = text;
    }

    public String text() {
        return text;
    }

    public Font font() {
        return font;
    }

    public float size() {
        return size;
    }

    public double density() {
        return density;
    }

    public World world() {
        return world;
    }

    public double baseX() {
        return baseX;
    }

    public double baseY() {
        return baseY;
    }

    public double baseZ() {
        return baseZ;
    }

    public float rotX() {
        return rotX;
    }

    public float rotY() {
        return rotY;
    }

    public float rotZ() {
        return rotZ;
    }

    public void drawPixel(double px, double py) {
        if (this.quaternion == null) {
            this.quaternion = new Quaternionf().rotationXYZ((float) Math.toRadians(rotX + 180),
                                                            (float) Math.toRadians(rotY),
                                                            (float) Math.toRadians(rotZ)
            );
        }

        var p = new Vector3f((float) px, (float) py, 0);

        this.quaternion.transform(p); // 四元数进行三维旋转
        this.count++;
        this.world.spawnParticle(Particle.END_ROD, baseX + p.x, baseY + p.y, baseZ + p.z, 0, 0, 0, 0,0,null,true);
    }

    public int getCount() {
        return this.count;
    }

    public Location location() {
        var loc=new Location(world(), baseX(),baseY(),baseZ());
        loc.setYaw(this.rotX());
        loc.setPitch(this.rotY());
        return loc;
    }
}
