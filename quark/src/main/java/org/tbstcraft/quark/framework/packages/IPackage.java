package org.tbstcraft.quark.framework.packages;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.data.config.Configuration;
import org.tbstcraft.quark.framework.data.config.Language;
import org.tbstcraft.quark.framework.module.providing.ModuleRegistry;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.ModuleManager;
import org.tbstcraft.quark.framework.service.providing.ServiceRegistry;
import org.tbstcraft.quark.util.ExceptionUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Logger;

public interface IPackage {
    static void registerAll(IPackage p, ClassLoader loader) {
        JsonObject modules = p.getPackageDescriptor().get("modules").getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : modules.entrySet()) {
            JsonElement entryElement = entry.getValue();
            String path = entryElement.getAsString();

            if (path.startsWith(".")) {
                path = p.getPackageDescriptor().get("package_namespace").getAsString() + path;
            }

            try {
                Class<?> clazz = loader.loadClass(path);
                PackageModule m = (PackageModule) clazz.getDeclaredConstructor().newInstance();
                m.init(entry.getKey(), p);
                ModuleManager.registerModule(m, p.getLogger());
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                ExceptionUtil.log(e);
                p.getLogger().warning("failed to construct module %s: %s".formatted(entry.getKey(), e.getMessage()));
            }
        }
        Quark.LOGGER.info("registered package %s(%s).".formatted(p.getClass().getName(), p.getId()));
    }

    static void unregisterAll(IPackage p) {
        JsonObject modules = p.getPackageDescriptor().get("modules").getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : modules.entrySet()) {
            ModuleManager.unregisterModule(p.getId() + ":" + entry.getKey(), p.getLogger());
        }
        Quark.LOGGER.info("unregistered package %s(%s).".formatted(p.getClass().getName(), p.getId()));
    }


    //---[attribute]---
    default JsonObject getPackageDescriptor(){
        return null;
    }

    String getId();

    QuarkPackage getDescriptor();

    String getLoggerName();


    //---[context]---
    Logger getLogger();

    Language getLanguageFile();

    Configuration getConfigFile();


    //---[lifecycle]---
    void onEnable();

    void onDisable();

    ModuleRegistry getModuleRegistry();

    ServiceRegistry getServiceRegistry();

    FeatureAvailability getAvailability();

    Plugin getOwner();
}
