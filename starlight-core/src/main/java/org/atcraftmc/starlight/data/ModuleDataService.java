package org.atcraftmc.starlight.data;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.modular.service.ServiceHolder;
import me.gb2022.modular.service.injection.ServiceInject;
import me.gb2022.modular.service.ServiceLayer;
import me.gb2022.modular.service.ApplicationService;
import me.gb2022.modular.service.injection.ServiceProvider;
import org.atcraftmc.starlight.data.storage.DataEntry;
import org.atcraftmc.starlight.framework.SLService;
import org.atcraftmc.starlight.util.FilePath;

import java.io.File;

@ApplicationService(id = "module-data",layer = ServiceLayer.FOUNDATION)
public interface ModuleDataService extends SLService {
    @ServiceInject
    ServiceHolder<ModuleDataService> INSTANCE = new ServiceHolder<>();

    @ServiceProvider
    static ModuleDataService create(){
        return create(FilePath.slDataFolder() + "/data/module");
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
        public void enable() {
            //DataFix.moveFolder("/module_data", "/data/module");
            this.open();
        }

        @Override
        public void disable() {
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
