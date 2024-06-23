package org.tbstcraft.quark.foundation.region;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.World;

public final class SimpleRegion extends Region {
    public SimpleRegion(World world, int x0, int y0, int z0, int x1, int y1, int z1) {
        super(world, x0, y0, z0, x1, y1, z1);
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
