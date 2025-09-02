package org.atcraftmc.starlight.framework.packages;

import me.gb2022.commons.TriState;
import org.atcraftmc.starlight.ProductInfo;
import org.atcraftmc.starlight.framework.service.*;
import org.atcraftmc.starlight.migration.DataFix;
import org.atcraftmc.starlight.util.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.foundation.platform.PluginUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Logger;

@SLService(id = "package", impl = PackageManager.Impl.class, layer = ServiceLayer.FRAMEWORK)
public interface PackageManager extends Service {
    String CORE_PKG_ID = "starlight-core";

    @ServiceInject
    ServiceHolder<PackageManager> INSTANCE = new ServiceHolder<>();

    static PackageManager getInstance() {
        return INSTANCE.get();
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
        for (File f : PluginUtil.getAllPluginFiles()) {
            if (!verify(f)) {
                continue;
            }
            list.add(f);
        }
        return list;
    }

    static boolean verify(Plugin p) {
        if (p.getName().equals(ProductInfo.CORE_ID)) {
            return false;
        }

        return p.getResource("product-meta.properties") != null;
    }

    static boolean verify(File f) {
        String id;
        try {
            id = PluginUtil.getPluginDescription(f).getName();
        } catch (InvalidDescriptionException e) {
            e.printStackTrace();
            return false;
        }

        if (id.equals(ProductInfo.CORE_ID)) {
            return false;
        }
        try {
            JarFile jf = new JarFile(f);
            if (jf.getJarEntry("product-meta.properties") == null) {
                jf.close();
                return false;
            }
            jf.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unused")
    static void reload() {
        for (String s : getSubPacksFromServer()) {
            PluginUtil.unload(s);
        }
        for (File f : getSubPacksFromFolder()) {
            PluginUtil.load(f.getName());
        }
    }

    static void registerPackage(AbstractPackage pkg) {
        INSTANCE.get().addPackage(pkg);
    }

    static void unregisterPackage(AbstractPackage pkg) {
        INSTANCE.get().removePackage(pkg.getId());
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

    static Set<AbstractPackage> getByStatus(TriState status) {
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

    static Set<String> getIdsByStatus(TriState status) {
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
        return getPackageStatus(id) == TriState.FALSE;
    }

    static TriState getPackageStatus(String id) {
        return INSTANCE.get().getStatus(id);
    }

    static Map<String, AbstractPackage> getAllPackages() {
        return INSTANCE.get().getPackages();
    }

    static void addRejection(String s) {

    }

    AbstractPackage get(String id);

    Map<String, AbstractPackage> getPackages();

    TriState getStatus(String id);

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
        private final Logger logger = Starlight.instance().getLogger();


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
                if (getStatus(pkg.getId()) == TriState.UNKNOWN) {
                    boolean enable = false;
                    if (pkg.getInitializer().isEnableByDefault()) {
                        enable = Starlight.instance().getConfig().getBoolean("config.default-status.package");
                    }

                    this.statusMap.put(pkg.getId(), enable ? "enabled" : "disabled");
                    this.saveStatus();
                }
                if (Identifiers.external(pkg.getId()).equals(CORE_PKG_ID)) {
                    this.statusMap.put(pkg.getId(), "enabled");
                }
                if (getStatus(pkg.getId()) == TriState.FALSE) {
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
            if (this.getStatus(id) == TriState.TRUE) {
                return;
            }
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
            if (getStatus(id) == TriState.UNKNOWN) {
                return ObjectOperationResult.NOT_FOUND;
            }
            if (getStatus(id) == TriState.FALSE) {
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
            if (getStatus(id) == TriState.UNKNOWN) {
                return ObjectOperationResult.NOT_FOUND;
            }
            if (getStatus(id) == TriState.TRUE) {
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
        public TriState getStatus(String id) {
            if (!this.statusMap.containsKey(id)) {
                return TriState.UNKNOWN;
            }
            return Objects.equals(this.statusMap.get(id), "enabled") ? TriState.FALSE : TriState.TRUE;
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
