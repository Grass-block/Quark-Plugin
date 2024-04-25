package org.tbstcraft.quark.service.framework;

import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.util.ObjectOperationResult;
import org.tbstcraft.quark.util.ObjectStatus;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.packages.AbstractPackage;
import org.tbstcraft.quark.service.Service;
import org.tbstcraft.quark.service.ServiceImplementation;
import org.tbstcraft.quark.util.ExceptionUtil;
import org.tbstcraft.quark.util.FilePath;
import org.tbstcraft.quark.util.container.ObjectContainer;
import org.tbstcraft.quark.util.api.BukkitPluginManager;
import org.tbstcraft.quark.util.api.BukkitUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

@ServiceImplementation(impl = PackageManager.Impl.class)
public interface PackageManager extends Service {
    ObjectContainer<PackageManager> INSTANCE = new ObjectContainer<>();

    static ModuleManager getInstance() {
        return ModuleManager.INSTANCE.get();
    }

    static List<String> getSubPacksFromServer() {
        List<String> list = new ArrayList<>();
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            if (!verifySubPackage(p)) {
                continue;
            }
            list.add(p.getName());
        }
        return list;
    }

    static List<File> getSubPacksFromFolder() {
        List<File> list = new ArrayList<>();
        for (File f : BukkitPluginManager.getAllPluginFiles()) {
            if (!verifySubPackage(f)) {
                continue;
            }
            list.add(f);
        }
        return list;
    }

    static boolean verifySubPackage(Plugin p) {
        if (!p.getName().startsWith(Quark.PLUGIN_ID)) {
            return false;
        }
        return !p.getName().equals(Quark.PLUGIN_ID);
    }

    static boolean verifySubPackage(File f) {
        String id;
        try {
            id = BukkitUtil.getPluginDescription(f).getName();
        } catch (InvalidDescriptionException e) {
            throw new RuntimeException(e);
        }

        if (!id.startsWith(Quark.PLUGIN_ID)) {
            return false;
        }
        return !id.equals(Quark.PLUGIN_ID);
    }

    @SuppressWarnings("unused")
    static void loadSubPacks() {
        for (String s : getSubPacksFromServer()) {
            BukkitPluginManager.unload(s);
        }
        for (File f : getSubPacksFromFolder()) {
            BukkitPluginManager.load(f.getName());
        }
    }


    static void init() {
        INSTANCE.set(new Impl());
        INSTANCE.get().onEnable();
    }

    static void stop() {
        INSTANCE.get().onDisable();
    }

    static void registerPackage(AbstractPackage pkg) {
        INSTANCE.get().addPackage(pkg);
    }

    static void unregisterPackage(AbstractPackage pkg) {
        INSTANCE.get().removePackage(pkg.getId());
    }

    static ModuleManager create(Plugin p) {
        return new ModuleManager.Impl(p);
    }

    static AbstractPackage getModule(String id) {
        return INSTANCE.get().get(id);
    }

    //operation
    static ObjectOperationResult enablePackage(String id) {
        return INSTANCE.get().enable(id);
    }

    static ObjectOperationResult disablePackage(String id) {
        return INSTANCE.get().disable(id);
    }

    static void enableAllPackages() {
        INSTANCE.get().enableAll();
    }

    static void disableAllPackages() {
        INSTANCE.get().disableAll();
    }

    static Set<AbstractPackage> getByStatus(ObjectStatus status) {
        Set<AbstractPackage> result = new HashSet<>();
        for (String id : INSTANCE.get().getPackages().keySet()) {
            if (getPackageStatus(id) != status) {
                continue;
            }
            result.add(getPackage(id));
        }
        return result;
    }

    static AbstractPackage getPackage(String id) {
        return INSTANCE.get().get(id);
    }

    static Set<String> getIdsByStatus(ObjectStatus status) {
        Set<String> result = new HashSet<>();
        for (String id : INSTANCE.get().getPackages().keySet()) {
            if (getPackageStatus(id) != status) {
                continue;
            }
            result.add(id);
        }
        return result;
    }

    //status
    static boolean isPackageEnabled(String id) {
        return getPackageStatus(id) == ObjectStatus.ENABLED;
    }

    static ObjectStatus getPackageStatus(String id) {
        return INSTANCE.get().getStatus(id);
    }

    static Map<String, AbstractPackage> getAllPackages() {
        return INSTANCE.get().getPackages();
    }

    AbstractPackage get(String id);

    Map<String, AbstractPackage> getPackages();

    ObjectStatus getStatus(String id);

    ObjectOperationResult enable(String id);

    ObjectOperationResult disable(String id);

    void addPackage(AbstractPackage pkg);

    void removePackage(String id);

    default void enableAll() {
        for (String id : this.getPackages().keySet()) {
            this.enable(id);
        }
    }

    default void disableAll() {
        for (String id : this.getPackages().keySet()) {
            this.disable(id);
        }
    }

    final class Impl implements PackageManager {
        private final Map<String, AbstractPackage> packages = new HashMap<>();
        private final Properties statusMap = new Properties();
        private final Logger logger = Quark.LOGGER;

        @Override
        public AbstractPackage get(String id) {
            return this.packages.get(id);
        }

        @Override
        public Map<String, AbstractPackage> getPackages() {
            return this.packages;
        }

        @Override
        public void addPackage(AbstractPackage pkg) {
            try {
                pkg.initializePackage();
                this.packages.put(pkg.getId(), pkg);
                if (getStatus(pkg.getId()) == ObjectStatus.UNREGISTERED) {
                    this.statusMap.put(pkg.getId(), Quark.CONFIG.getConfig("default-status").getBoolean("package") ? "enabled" : "disabled");
                    this.saveStatus();
                }
                if (getStatus(pkg.getId()) == ObjectStatus.ENABLED) {
                    try {
                        pkg.onEnable();
                    } catch (Exception ex) {
                        ExceptionUtil.log(ex);
                    }
                }
            } catch (Exception e) {
                ExceptionUtil.log(e);
            }
        }

        @Override
        public void removePackage(String id) {
            if (!this.packages.containsKey(id)) {
                return;
            }
            this.packages.get(id).onDisable();
            this.packages.remove(id);
        }


        private void saveStatus() {
            try {
                this.statusMap.store(new FileOutputStream(this.getStatusFile()), "auto generated file,please don't edit it.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private File getStatusFile() {
            String path = FilePath.pluginFolder("quark") + "/config/packages.properties";
            File file = new File(path);
            if (!file.exists() || file.length() == 0) {
                if (file.getParentFile().mkdirs()) {
                    this.logger.info("created package status file folder.");
                }
                try {
                    if (file.createNewFile()) {
                        this.logger.info("created package status file.");
                    }
                } catch (IOException e) {
                    this.logger.severe("failed to create package status file");
                    return file;
                }
                return file;
            }
            return file;
        }

        private ObjectOperationResult enable0(String id) {
            if (getStatus(id) == ObjectStatus.UNREGISTERED) {
                return ObjectOperationResult.NOT_FOUND;
            }
            if (getStatus(id) == ObjectStatus.ENABLED) {
                return ObjectOperationResult.ALREADY_OPERATED;
            }
            try {
                this.get(id).onEnable();
            } catch (Exception ex) {
                ExceptionUtil.log(ex);
                return ObjectOperationResult.INTERNAL_ERROR;
            }
            this.statusMap.put(id, "enabled");
            return ObjectOperationResult.SUCCESS;
        }

        private ObjectOperationResult disable0(String id) {
            if (getStatus(id) == ObjectStatus.UNREGISTERED) {
                return ObjectOperationResult.NOT_FOUND;
            }
            if (getStatus(id) == ObjectStatus.DISABLED) {
                return ObjectOperationResult.ALREADY_OPERATED;
            }
            try {
                this.get(id).onDisable();
            } catch (Exception ex) {
                ExceptionUtil.log(ex);
                return ObjectOperationResult.INTERNAL_ERROR;
            }
            this.statusMap.put(id, "disabled");
            return ObjectOperationResult.SUCCESS;
        }


        @Override
        public ObjectOperationResult enable(String id) {
            ObjectOperationResult result = enable0(id);
            if (result == ObjectOperationResult.SUCCESS) {
                this.logger.info("enabled module %s.".formatted(id));
            }
            this.saveStatus();
            return result;
        }

        @Override
        public ObjectOperationResult disable(String id) {
            ObjectOperationResult result = disable0(id);
            if (result == ObjectOperationResult.SUCCESS) {
                this.logger.info("disabled module %s.".formatted(id));
            }
            this.saveStatus();
            return result;
        }

        @Override
        public ObjectStatus getStatus(String id) {
            if (!this.statusMap.containsKey(id)) {
                return ObjectStatus.UNREGISTERED;
            }
            return Objects.equals(this.statusMap.get(id), "enabled") ? ObjectStatus.ENABLED : ObjectStatus.DISABLED;
        }

        @Override
        public void onEnable() {
            try {
                this.statusMap.load(new FileInputStream(this.getStatusFile()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onDisable() {
            this.saveStatus();
            for (String id : new ArrayList<>(this.getPackages().keySet())) {
                this.removePackage(id);
            }
        }
    }
}
