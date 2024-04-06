package org.tbstcraft.quark.service.data;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.service.Service;
import org.tbstcraft.quark.util.FilePath;
import org.tbstcraft.quark.util.ObjectContainer;

import java.io.File;

public interface ModuleDataService extends Service {
    ObjectContainer<ModuleDataService> INSTANCE = new ObjectContainer<>();

    static void init() {
        INSTANCE.set(create(FilePath.moduleData(Quark.PLUGIN_ID)));
        INSTANCE.get().onEnable();
    }

    static void stop() {
        INSTANCE.get().onDisable();
    }

    static NBTTagCompound getEntry(String id) {
        return INSTANCE.get().getDataEntry(id);
    }

    static void save(String id) {
        INSTANCE.get().saveData(id);
    }

    static ModuleDataService create(String folder) {
        return new ServiceImplementation(new File(folder));
    }

    static int getEntryCount() {
        return INSTANCE.get().entryCount();
    }


    int entryCount();

    NBTTagCompound getDataEntry(String id);

    void saveData(String id);

    DataService getBackend();

    final class ServiceImplementation implements ModuleDataService {
        private final DataService backend;

        public ServiceImplementation(File f) {
            this.backend = new DataService(Quark.LOGGER, f);
        }

        @Override
        public void onEnable() {
            this.backend.open();
        }

        @Override
        public void onDisable() {
            this.backend.close();
        }

        @Override
        public NBTTagCompound getDataEntry(String id) {
            return this.backend.getEntry(id);
        }

        @Override
        public void saveData(String id) {
            this.backend.saveEntry(id);
        }

        @Override
        public int entryCount() {
            return this.backend.getEntryCount();
        }

        public DataService getBackend() {
            return backend;
        }
    }
}
