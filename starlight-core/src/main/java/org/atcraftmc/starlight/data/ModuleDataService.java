package org.atcraftmc.starlight.data;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.atcraftmc.starlight.data.storage.DataEntry;
import org.atcraftmc.starlight.framework.service.*;
import org.atcraftmc.starlight.util.FilePath;

import java.io.File;

@SLService(id = "module-data",layer = ServiceLayer.FOUNDATION)
public interface ModuleDataService extends Service {
    ServiceHolder<ModuleDataService> INSTANCE = new ServiceHolder<>();

    @ServiceInject
    static void start() throws Exception {
        INSTANCE.set(create(FilePath.slDataFolder() + "/data/module"));
        INSTANCE.get().onEnable();
    }

    @ServiceInject
    static void stop() throws Exception {
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

    static DataEntry get(String id) {
        return INSTANCE.get().getData(id);
    }


    int entryCount();

    NBTTagCompound getDataEntry(String id);

    DataEntry getData(String id);

    void saveData(String id);

    final class ServiceImplementation extends DataService implements ModuleDataService {
        public ServiceImplementation(File f) {
            super(f);
        }

        @Override
        public void onEnable() {
            //DataFix.moveFolder("/module_data", "/data/module");
            this.open();
        }

        @Override
        public void onDisable() {
            this.close();
        }

        @Override
        public NBTTagCompound getDataEntry(String id) {
            return getEntry(id);
        }

        @Override
        public DataEntry getData(String id) {
            return this.get(id);
        }

        @Override
        public void saveData(String id) {
            this.saveEntry(id);
        }

        @Override
        public int entryCount() {
            return this.getEntryCount();
        }
    }
}
