package org.tbstcraft.quark.pkg;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.config.ConfigFile;
import org.tbstcraft.quark.config.LanguageFile;
import org.tbstcraft.quark.module.ModuleManager;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.util.FilePath;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public abstract class PluginPackage extends JavaPlugin {
    private String id;
    private JsonObject descriptor;
    private LanguageFile languageFile;
    private ConfigFile configFile;

    public static void registerAll(PluginPackage p) {
        JsonObject modules = p.getDescriptor().get("modules").getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : modules.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }
            if (!entry.getValue().getAsJsonObject().has("class")) {
                continue;
            }
            try {
                Class<?> clazz = Class.forName(entry.getValue().getAsJsonObject().get("class").getAsString());
                PluginModule m = (PluginModule) clazz.getDeclaredConstructor().newInstance();
                m.initializeModule(entry.getKey(), p, entry.getValue().getAsJsonObject());
                ModuleManager.register(m);
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void unregisterAll(PluginPackage p) {
        JsonObject modules = p.getDescriptor().get("modules").getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : modules.entrySet()) {
            ModuleManager.unregister(p.id + ":" + entry.getKey());
        }
    }

    public JsonObject getDescriptor() {
        return this.descriptor;
    }

    public String getId() {
        return this.id;
    }

    public LanguageFile getLanguageFile() {
        return this.languageFile;
    }

    public ConfigFile getConfigFile() {
        return configFile;
    }

    @Override
    public void onEnable() {
        this.id = this.getPackageId();
        this.descriptor = FilePath.packageDescriptor(this.id);
        this.languageFile = new LanguageFile(this.id);
        if (!this.descriptor.has("config") || this.descriptor.get("config").getAsBoolean()) {
            this.configFile = new ConfigFile(this.getId());
        }
        registerAll(this);
    }

    public void onDisable() {
        unregisterAll(this);
    }

    public abstract String getPackageId();
}
