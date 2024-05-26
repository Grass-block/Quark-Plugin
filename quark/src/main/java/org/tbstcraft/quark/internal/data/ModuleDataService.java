package org.tbstcraft.quark.internal.data;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceHolder;
import org.tbstcraft.quark.framework.service.ServiceInject;
import org.tbstcraft.quark.util.DataFix;
import org.tbstcraft.quark.util.FilePath;

import java.io.File;
import java.util.Objects;

@QuarkService(id = "module-data")
public interface ModuleDataService extends Service {
    ServiceHolder<ModuleDataService> INSTANCE = new ServiceHolder<>();

    @ServiceInject
    static void start() {
        INSTANCE.set(create(FilePath.pluginFolder("Quark") + "/data/module"));
        INSTANCE.get().onEnable();
    }

    @ServiceInject
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
            DataFix.moveFolder("/module_data","/data/module");
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
