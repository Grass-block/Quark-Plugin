package org.atcraftmc.starlight.framework.packages;

import me.gb2022.commons.TriState;
import me.gb2022.modular.ObjectOperationResult;
import me.gb2022.modular.pack.AbstractPackageManager;
import me.gb2022.modular.pack.PackageManager;
import me.gb2022.modular.service.ApplicationService;
import me.gb2022.modular.service.ServiceHolder;
import me.gb2022.modular.service.ServiceLayer;
import me.gb2022.modular.service.injection.ServiceInject;
import me.gb2022.modular.service.injection.ServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.starlight.ProductInfo;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.foundation.platform.PluginUtil;
import org.atcraftmc.starlight.framework.SLService;
import org.atcraftmc.starlight.migration.DataFix;
import org.atcraftmc.starlight.util.FilePath;
import org.atcraftmc.starlight.util.Identifiers;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;

@ApplicationService(id = "package", impl = SLPackageManager.Impl.class, layer = ServiceLayer.FRAMEWORK)
public interface SLPackageManager extends PackageManager<SLPackage>, SLService {
    Logger LOGGER = LogManager.getLogger("SLPackageManager");

    String CORE_PKG_ID = "starlight-core";

    @ServiceInject
    ServiceHolder<SLPackageManager> INSTANCE = new ServiceHolder<>();

    @ServiceProvider
    static SLPackageManager create() {
        return new Impl();
    }

    static SLPackageManager getInstance() {
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

        return p.getResource("product-meta.json") != null;
    }

    static boolean verify(File f) {
        String id;
        try {
            id = PluginUtil.getPluginDescription(f).getName();
        } catch (InvalidDescriptionException e) {
            LOGGER.catching(e);
            return false;
        }

        if (id.equals(ProductInfo.CORE_ID)) {
            return false;
        }
        try {
            JarFile jf = new JarFile(f);
            if (jf.getJarEntry("product-meta.json") == null) {
                jf.close();
                return false;
            }
            jf.close();

            return true;
        } catch (IOException e) {
            LOGGER.catching(e);
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

    static void registerPackage(SLPackage pkg) {
        INSTANCE.get().addPackage(pkg);
    }

    static void unregisterPackage(SLPackage pkg) {
        INSTANCE.get().removePackage(pkg.getId());
    }

    static SLPackage getModule(String id) {
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

    static Set<SLPackage> getByStatus(TriState status) {
        var result = new HashSet<SLPackage>();
        for (String id : INSTANCE.get().getPackages().keySet()) {
            if (getPackageStatus(id) != status) {
                continue;
            }
            result.add(getPackage(id));
        }
        return result;
    }

    static SLPackage getPackage(String id) {
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

    static Map<String, SLPackage> getAllPackages() {
        return INSTANCE.get().getPackages();
    }

    static void addRejection(String s) {
    }

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

    final class Impl extends AbstractPackageManager<SLPackage> implements SLPackageManager {
        @Override
        public void saveStatus(Properties meta) {
            try {
                meta.store(new FileOutputStream(this.getStatusFile()), "auto generated file,please don't edit it.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void handleException(Throwable e) {
                e.printStackTrace();
        }

        @Override
        public Logger getLogger() {
            return LOGGER;
        }

        @Override
        public boolean isReservedPackage(SLPackage pack) {
            if (pack == null) {
                return false;
            }
            return Identifiers.external(pack.getId()).equals(CORE_PKG_ID);
        }

        @Override
        public boolean defaultPackageStatus(SLPackage pack) {
            if (pack == null) {
                return false;
            }
            if (pack.getInitializer().isEnableByDefault()) {
                return Starlight.instance().getConfig().getBoolean("config.default-status.package");
            }

            return false;
        }

        private File getStatusFile() {
            String path = FilePath.pluginFolder("quark") + "/data/packages.properties";
            File file = new File(path);
            if (!file.exists() || file.length() == 0) {
                if (file.getParentFile().mkdirs()) {
                    LOGGER.info("created package status file folder.");
                }
                try {
                    if (file.createNewFile()) {
                        LOGGER.info("created package status file.");
                    }
                } catch (IOException e) {
                    LOGGER.error("failed to create package status file");
                    return file;
                }
                return file;
            }
            return file;
        }

        @Override
        public void enable() {
            DataFix.moveFile("/config/packages.properties", "/data/packages.properties");
            try {
                this.statusMap.load(new FileInputStream(this.getStatusFile()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
