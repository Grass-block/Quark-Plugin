package org.tbstcraft.quark.framework.packages;

import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.platform.BukkitPluginManager;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.framework.module.ModuleManager;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceHolder;
import org.tbstcraft.quark.framework.service.ServiceInject;
import org.tbstcraft.quark.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Logger;

@QuarkService(id = "package", impl = PackageManager.Impl.class)
public interface PackageManager extends Service {
    String CORE_PKG_ID = "quark-core";

    @ServiceInject
    ServiceHolder<PackageManager> INSTANCE = new ServiceHolder<>();

    static ModuleManager getInstance() {
        return ModuleManager.INSTANCE.get();
    }

    static List<String> getSubPacksFromServer() {
        List<String> list = new ArrayList<>();
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            if (!verify(p)) {
                continue;
            }
            list.add(p.getName());
        }
        return list;
    }

    static List<File> getSubPacksFromFolder() {
        List<File> list = new ArrayList<>();
        for (File f : BukkitPluginManager.getAllPluginFiles()) {
            if (!verify(f)) {
                continue;
            }
            list.add(f);
        }
        return list;
    }

    static boolean verify(Plugin p) {
        if (!p.getName().startsWith(Quark.PLUGIN_ID)) {
            return false;
        }

        if (p.getName().equals(Quark.PLUGIN_ID)) {
            return false;
        }

        return p.getResource("product-info.properties") != null;
    }

    static boolean verify(File f) {
        String id;
        try {
            id = BukkitUtil.getPluginDescription(f).getName();
        } catch (InvalidDescriptionException e) {
            throw new RuntimeException(e);
        }

        if (!id.startsWith(Quark.PLUGIN_ID)) {
            return false;
        }
        if (id.equals(Quark.PLUGIN_ID)) {
            return false;
        }
        try {
            JarFile jf = new JarFile(f);
            if (jf.getJarEntry("product-info.properties") == null) {
                jf.close();
                return false;
            }
            jf.close();

            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    static void reload() {
        for (String s : getSubPacksFromServer()) {
            BukkitPluginManager.unload(s);
        }
        for (File f : getSubPacksFromFolder()) {
            BukkitPluginManager.load(f.getName());
        }
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

    static void addRejection(String s) {

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
            if (Identifiers.external(id).equals(CORE_PKG_ID)) {
                continue;
            }
            this.enable(id);
        }
    }

    default void disableAll() {
        for (String id : this.getPackages().keySet()) {
            if (Identifiers.external(id).equals(CORE_PKG_ID)) {
                continue;
            }
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
                    this.statusMap.put(pkg.getId(), Quark.PLUGIN.getConfig().getBoolean("config.default-status.package") ? "enabled" : "disabled");
                    this.saveStatus();
                }
                if (Identifiers.external(pkg.getId()).equals(CORE_PKG_ID)) {
                    this.statusMap.put(pkg.getId(), "enabled");
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
            String path = FilePath.pluginFolder("quark") + "/data/packages.properties";
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
            if (Identifiers.external(id).equals(CORE_PKG_ID)) {
                return ObjectOperationResult.BLOCKED_INTERNAL;
            }
            ObjectOperationResult result = enable0(id);
            if (result == ObjectOperationResult.SUCCESS) {
                this.logger.info("enabled module %s.".formatted(id));
            }
            this.saveStatus();
            return result;
        }

        @Override
        public ObjectOperationResult disable(String id) {
            if (Identifiers.external(id).equals(CORE_PKG_ID)) {
                return ObjectOperationResult.BLOCKED_INTERNAL;
            }
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
            DataFix.moveFile("/config/packages.properties", "/data/packages.properties");
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
