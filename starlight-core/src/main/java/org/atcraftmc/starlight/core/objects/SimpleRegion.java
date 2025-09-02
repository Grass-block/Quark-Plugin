package org.atcraftmc.starlight.core.objects;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.World;

public final class SimpleRegion extends Region {
    public SimpleRegion(World world, int x0, int y0, int z0, int x1, int y1, int z1) {
        super(world, x0, y0, z0, x1, y1, z1);
    }

    public SimpleRegion(Location p0, Location p1) {
        super(p0.getWorld());
        this.setPoint0(p0);
        this.setPoint1(p1);
    }

    public SimpleRegion(NBTTagCompound tag) {
        super(tag);
    }

    @Override
    public void readAdditionData(NBTTagCompound addition) {

    }

    @Override
    public void writeAdditionData(NBTTagCompound addition) {

    }
}
