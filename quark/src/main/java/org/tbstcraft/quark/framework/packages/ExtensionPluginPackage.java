package org.tbstcraft.quark.framework.packages;

import com.google.gson.JsonObject;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.framework.data.config.Configuration;
import org.tbstcraft.quark.framework.data.config.Language;

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
            this.configFile = new Configuration(this.locale());
        }
        //IPackage.registerAll(this, this.getClass().getClassLoader());

         */
    }

    @Override
    public final void onDisable() {
    }


    @Override
    public final JsonObject getPackageDescriptor() {
        return this.descriptor;
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

