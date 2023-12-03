package org.tbstcraft.quark.module;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.command.AbstractCommand;
import org.tbstcraft.quark.command.CommandManager;
import org.tbstcraft.quark.config.LanguageEntry;
import org.tbstcraft.quark.pkg.PluginPackage;
import org.tbstcraft.quark.record.RecordEntry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public abstract class PluginModule implements Listener {
    public Logger logger;
    private PluginPackage parent;
    private JsonObject descriptor;
    private RecordEntry recordEntry;
    private LanguageEntry languageEntry;
    private String id;

    public void initializeModule(String id, PluginPackage parent, JsonObject object) {
        this.id = id;
        this.parent = parent;
        this.descriptor = object;
        this.languageEntry = this.parent.getLanguageFile().getEntry(this.getId());
        this.logger = parent.getLogger();
        this.recordEntry = new RecordEntry();
    }

    //module
    public void onEnable() {
    }

    public void onDisable() {
    }


    //register
    public final void registerListener() {
        Bukkit.getPluginManager().registerEvents(this, Quark.PLUGIN);
    }

    public final void unregisterListener() {
        for (Method m : this.getClass().getMethods()) {
            EventHandler handler = m.getAnnotation(EventHandler.class);
            if (handler == null) {
                continue;
            }
            HandlerList list;
            try {
                Class<?> clazz = m.getParameters()[0].getType();

                list = (HandlerList) clazz.getMethod("getHandlerList").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                this.logger.warning("failed to unregister listener: " + e.getMessage());
                continue;
            }
            list.unregister(this);
        }
    }

    public final void registerCommand(AbstractCommand command) {
        CommandManager.registerCommand(command);
    }

    public final void unregisterCommand(AbstractCommand command) {
        CommandManager.unregisterCommand(command);
    }


    //service
    public final ConfigurationSection getConfig() {
        return this.parent.getConfigFile().getConfig(this.getId());
    }

    public final LanguageEntry getLanguage() {
        return this.languageEntry;
    }

    public final RecordEntry getRecordEntry() {
        return this.recordEntry;
    }


    //access
    public JsonObject getDescriptor() {
        return this.descriptor;
    }

    public PluginPackage getParent() {
        return this.parent;
    }

    public final String getId() {
        return this.id;
    }

    public final String getVersion() {
        return this.descriptor.get("version").getAsString();
    }

    public String getFullId() {
        return this.getParent().getId() + ":" + this.getId();
    }
}
