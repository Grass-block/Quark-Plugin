package org.tbstcraft.quark.util.region;

import me.gb2022.commons.nbt.NBTTagCompound;

import java.util.Set;

public interface RegionDataManager {
    static RegionDataManager NBT(NBTTagCompound tag) {
        return null;
    }

    void save(String id, NBTTagCompound tag);

    NBTTagCompound load(String id);

    Set<String> list();

    class NBTDataManager implements RegionDataManager {

        @Override
        public void save(String id, NBTTagCompound tag) {

        }

        @Override
        public NBTTagCompound load(String id) {
            return null;
        }

        @Override
        public Set<String> list() {
            return null;
        }
    }
}
