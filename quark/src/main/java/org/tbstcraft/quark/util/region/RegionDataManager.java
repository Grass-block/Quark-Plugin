package org.tbstcraft.quark.util.region;

import me.gb2022.commons.nbt.NBT;
import me.gb2022.commons.nbt.NBTTagCompound;

import java.util.Set;

public interface RegionDataManager {
    static RegionDataManager NBT(NBTTagCompound tag) {
        return new NBTDataManager(tag);
    }

    void save(String id, NBTTagCompound tag);

    NBTTagCompound load(String id);

    Set<String> list();

    class NBTDataManager implements RegionDataManager {
        private final NBTTagCompound base;

        public NBTDataManager(NBTTagCompound base) {
            this.base = base;
        }

        @Override
        public void save(String id, NBTTagCompound tag) {
            this.base.setTag(id, tag);
        }

        @Override
        public NBTTagCompound load(String id) {
            return this.base.getCompoundTag(id);
        }

        @Override
        public Set<String> list() {
            return this.base.getTagMap().keySet();
        }
    }
}
