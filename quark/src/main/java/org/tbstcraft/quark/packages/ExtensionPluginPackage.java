package org.tbstcraft.quark.packages;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.config.Configuration;
import org.tbstcraft.quark.config.Language;
import org.tbstcraft.quark.util.FilePath;
import org.tbstcraft.quark.util.Timer;

import java.util.Objects;
import java.util.logging.Logger;

@Deprecated
public abstract class ExtensionPluginPackage extends JavaPlugin implements IPackage{
    private String id;
    private JsonObject descriptor;
    private Language languageFile;
    private Configuration configFile;
    private String coreInstanceId;

    @Override
    public QuarkPackage getDescriptor() {
        return this.getClass().getAnnotation(QuarkPackage.class);
    }

    @Override
    public final String getId() {
        return this.id;
    }

    @Override
    public final Language getLanguageFile() {
        return this.languageFile;
    }

    @Override
    public final Configuration getConfigFile() {
        return configFile;
    }

    @Override
    public String getLoggerName() {
        return this.getName();
    }

    @Override
    public final void onEnable() {
        /*
        if (this.validateCore()) {
            return;
        }

        Timer.restartTiming();

        this.coreInstanceId = Quark.PLUGIN.getInstanceId();

        this.id = this.getPackageId();
        this.descriptor = FilePath.packageDescriptor(this.id);
        this.languageFile = Language.create(this.id);
        if (!this.descriptor.has("config") || this.descriptor.get("config").getAsBoolean()) {
            this.configFile = new Configuration(this.getId());
        }
        //IPackage.registerAll(this, this.getClass().getClassLoader());

         */
    }

    @Override
    public final void onDisable() {
        if (!isCoreContextMatch()) {
            return;
        }

        Timer.restartTiming();
        //IPackage.unregisterAll(this);
    }


    @Override
    public final JsonObject getPackageDescriptor() {
        return this.descriptor;
    }

    public final boolean validateCore() {
        Logger logger = this.getLogger();

        if (Quark.isCoreUnavailable()) {
            logger.severe("cannot detect running quark core. consider reload your server.");
            return true;
        } else {
            return false;
        }
    }

    private boolean isCoreContextMatch() {
        Plugin quarkInstance = Bukkit.getPluginManager().getPlugin(Quark.PLUGIN_ID);
        if (quarkInstance == null) {
            return false;
        }
        String newId;
        try {
            newId = (String) quarkInstance.getClass().getMethod("getInstanceId").invoke(quarkInstance);
        } catch (Exception e) {
            return false;
        }
        return Objects.equals(this.coreInstanceId, newId);
    }

    public final String getPackageId() {
        return this.getClass().getAnnotation(QuarkPackage.class).value();
    }

    //ban bukkit plugin methods
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("why");
    }

    @Override
    public Plugin getOwner() {
        return this;
    }
}

