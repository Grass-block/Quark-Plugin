package org.tbstcraft.quark.service;

import org.tbstcraft.quark.data.DataBackend;
import org.tbstcraft.quark.data.FileBackend;
import org.tbstcraft.quark.data.LevelDBBackend;
import org.tbstcraft.quark.util.FilePath;
import org.tbstcraft.quark.util.nbt.NBTTagCompound;

public interface ModuleDataService {
    DataBackend BACKEND = new FileBackend(FilePath.moduleData());

    static NBTTagCompound getEntry(String id) {
        return BACKEND.getEntry(id);
    }

    static void save(String id) {
        BACKEND.save(id);
    }

    static void init() {
        BACKEND.open();
    }

    static void stop() {
        BACKEND.close();
    }
}
