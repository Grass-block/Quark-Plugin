package org.tbstcraft.quark.framework.module;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.foundation.command.AbstractCommand;
import org.tbstcraft.quark.data.language.ILanguageAccess;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.framework.module.compat.CompatContainer;
import org.tbstcraft.quark.framework.module.services.ModuleServices;
import org.tbstcraft.quark.framework.packages.IPackage;
import org.tbstcraft.quark.framework.record.EmptyRecordEntry;
import org.tbstcraft.quark.framework.record.RecordEntry;
import org.tbstcraft.quark.foundation.platform.APIProfile;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public abstract class AbstractModule implements Listener {
    private final Set<AbstractCommand> commands = new HashSet<>();
    private final Map<Class<?>, CompatContainer<?>> compatContainers = new HashMap<>();

    private RecordEntry record;
    private LanguageEntry language;
    private ConfigurationSection config;
    private Logger logger;

    public AbstractModule() {
    }


    //api
    public void enable() {
    }

    public void disable() {
    }

    @SuppressWarnings("RedundantThrows")
    public void checkCompatibility() throws Throwable {
    }


    public final void enableModule() {
        this.record = this.useRecord() ? createRecord() : new EmptyRecordEntry();
        this.language = this.useLanguage() ? createLanguage() : null;
        this.config = this.createConfig();
        this.logger = createLogger();

        ModuleServices.init(this);

        this.enable();
    }

    public final void disableModule() {
        this.disable();
        if (this.record != null) {
            this.record.close();
        }
        ModuleServices.disable(this);
    }


    //data support
    public abstract LanguageEntry createLanguage();

    public abstract RecordEntry createRecord();

    public abstract ConfigurationSection createConfig();

    public abstract Logger createLogger();


    //internal objects
    public final RecordEntry getRecord() {
        return this.record;
    }

    public final LanguageEntry getLanguage() {
        return this.language;
    }

    public final ConfigurationSection getConfig() {
        return this.config;
    }

    public final Logger getLogger() {
        return this.logger;
    }

    public final Set<AbstractCommand> getCommands() {
        return this.commands;
    }

    public final Map<Class<?>, CompatContainer<?>> getCompatContainers() {
        return this.compatContainers;
    }


    //attributes
    public final QuarkModule getDescriptor() {
        return this.getClass().getAnnotation(QuarkModule.class);
    }

    public String getFullId() {
        return getId();
    }

    public abstract String getId();


    public final String[] getRecordFormat() {
        return this.getDescriptor().recordFormat();
    }

    public final String getVersion() {
        return this.getDescriptor().version() + (this.isBeta() ? " - beta" : "");
    }

    public final boolean isBeta() {
        return this.getDescriptor().beta();
    }

    public final boolean useRecord() {
        return this.getDescriptor().recordFormat().length != 0;
    }

    public final boolean useLanguage() {
        return this.getDescriptor().useLanguage();
    }

    public final APIProfile[] getCompatBlacklist() {
        return this.getDescriptor().compatBlackList();
    }

    public final FeatureAvailability getAvailability() {
        FeatureAvailability availability = this.getDescriptor().available();
        if (availability != FeatureAvailability.INHERIT) {
            return availability;
        }
        return this.getParent().getAvailability();
    }


    //object
    @Override
    public final String toString() {
        return "%s{id=%s version=%s beta=%s, compat_blacklist=%s}".formatted(
                this.getClass().getSimpleName(),
                this.getVersion(),
                this.getFullId(),
                this.isBeta(),
                Arrays.toString(this.getCompatBlacklist())
        );
    }

    @Override
    public final int hashCode() {
        return this.getFullId().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof AbstractModule m)) {
            return false;
        }
        return Objects.equals(m.getFullId(), this.getFullId());
    }

    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("can't clone a module instance");
    }


    public abstract IPackage getParent();

    public final InputStream getResource(String path) {
        if (!path.startsWith("/")) {
            path = path + "/";
        }
        return this.getClass().getResourceAsStream("/assets" + path);
    }

    public final URL getResourceURL(String path) {
        return this.getClass().getResource("/assets" + path);
    }

    public Plugin getOwnerPlugin() {
        return this.getParent().getOwner();
    }

    public String getDisplayName(Locale locale) {
        ILanguageAccess lang = this.getParent().getLanguageFile();

        if (!lang.hasKey("_module-name", this.getId())) {
            return this.getId();
        }

        String displayName = lang.getMessage(locale, "_module-name", this.getId());
        return "%s{#gray}({#white}%s{#gray})".formatted(getId(), displayName);
    }
}
