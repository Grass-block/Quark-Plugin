package org.tbstcraft.quark.data;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.storage.DataEntry;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceHolder;
import org.tbstcraft.quark.framework.service.ServiceInject;
import org.tbstcraft.quark.util.DataFix;
import org.tbstcraft.quark.util.FilePath;

import java.io.File;

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

    static DataEntry get(String id) {
        return INSTANCE.get().getData(id);
    }


    int entryCount();

    NBTTagCompound getDataEntry(String id);

    DataEntry getData(String id);

    void saveData(String id);

    final class ServiceImplementation extends DataService implements ModuleDataService {
        public ServiceImplementation(File f) {
            super(Quark.getInstance().getLogger(), f);
        }

        @Override
        public void onEnable() {
            DataFix.moveFolder("/module_data", "/data/module");
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
