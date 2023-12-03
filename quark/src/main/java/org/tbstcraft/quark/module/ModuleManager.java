package org.tbstcraft.quark.module;

import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.util.FilePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

public interface ModuleManager {
    Properties STATUS_MAPPING = new Properties();
    Logger LOGGER = Quark.PLUGIN.getLogger();
    HashMap<String, PluginModule> MODULES = new HashMap<>();

    static PluginModule get(String id) {
        return MODULES.get(id);
    }

    //operation
    static void enable(String id) {
        if (getModuleStatus(id) != ModuleStatus.DISABLED) {
            return;
        }
        get(id).onEnable();
        LOGGER.info("enabled module %s.".formatted(id));
        STATUS_MAPPING.put(id, "enabled");
    }

    static void disable(String id) {
        if (getModuleStatus(id) != ModuleStatus.ENABLED) {
            return;
        }
        LOGGER.info("disabled module %s.".formatted(id));
        get(id).onDisable();
        STATUS_MAPPING.put(id, "disabled");
    }

    static void reload(String id) {
        disable(id);
        enable(id);
        LOGGER.info("reloaded module %s.".formatted(id));
    }

    static void enableAll() {
        for (String s : MODULES.keySet()) {
            enable(s);
        }
        LOGGER.info("enabled all modules.");
        reloadStatus();
    }

    static void disableAll() {
        for (String s : MODULES.keySet()) {
            disable(s);
        }
        LOGGER.info("disabled all modules.");
        reloadStatus();
    }

    static void reloadAll() {
        for (String s : MODULES.keySet()) {
            reload(s);
        }
        LOGGER.info("reloaded all modules.");
    }

    static void shutdown() {
        for (String s : MODULES.keySet()) {
            if (getModuleStatus(s) != ModuleStatus.ENABLED) {
                continue;
            }
            get(s).onDisable();
        }
    }

    //register
    static void register(PluginModule m) {
        MODULES.put(m.getFullId(), m);
        if (getModuleStatus(m.getFullId()) == ModuleStatus.UNREGISTERED) {
            STATUS_MAPPING.put(m.getFullId(), Quark.CONFIG.getConfig("module").getBoolean("default_status") ? "enabled" : "disabled");
            storeStatus();
        }
        if (getModuleStatus(m.getFullId()) == ModuleStatus.ENABLED) {
            m.onEnable();
        }
        LOGGER.info("registered module %s.".formatted(m.getId()));
    }

    static void unregister(String moduleId) {
        if (getModuleStatus(moduleId) == ModuleStatus.ENABLED) {
            MODULES.get(moduleId).onDisable();
        }
        MODULES.remove(moduleId);
        LOGGER.info("unregistered module %s.".formatted(moduleId));
    }

    //status
    static boolean isEnabled(String moduleId) {
        return getModuleStatus(moduleId) == ModuleStatus.ENABLED;
    }

    static ModuleStatus getModuleStatus(String moduleId) {
        if (!STATUS_MAPPING.containsKey(moduleId)) {
            return ModuleStatus.UNREGISTERED;
        }
        return Objects.equals(STATUS_MAPPING.get(moduleId), "enabled") ? ModuleStatus.ENABLED : ModuleStatus.DISABLED;
    }

    static void storeStatus() {
        try {
            STATUS_MAPPING.store(new FileOutputStream(FilePath.pluginFolder() + "/modules.properties"), "auto generated file,please don't edit it.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void loadStatus() {
        File f = new File(FilePath.pluginFolder() + "/modules.properties");
        if (!f.exists() || f.length() == 0) {
            f.getParentFile().mkdirs();
            try {
                f.createNewFile();
            } catch (IOException e) {
                LOGGER.severe("failed to create status file");
                return;
            }
            return;
        }
        try {
            STATUS_MAPPING.load(new FileInputStream(FilePath.pluginFolder() + "/modules.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void reloadStatus(){
        storeStatus();
        loadStatus();
    }
}
