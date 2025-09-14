package org.atcraftmc.starlight.framework.module;

import org.atcraftmc.qlib.command.AbstractCommand;
import org.atcraftmc.qlib.config.ConfigContainer;
import org.atcraftmc.qlib.config.ConfigEntry;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.data.record.EmptyRecordEntry;
import org.atcraftmc.starlight.data.record.RecordEntry;
import org.atcraftmc.starlight.data.record.RecordService;
import org.atcraftmc.starlight.foundation.platform.APIProfile;
import org.atcraftmc.starlight.framework.FeatureAvailability;
import org.atcraftmc.starlight.framework.FunctionalComponent;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.ModuleServices;
import org.atcraftmc.starlight.framework.packages.IPackage;
import org.atcraftmc.starlight.util.Identifiers;
import org.bukkit.plugin.Plugin;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public abstract class AbstractModule implements FunctionalComponent {
    private final Set<AbstractCommand> commands = new HashSet<>();
    private final Map<Class<? extends ModuleComponent<?>>, ModuleComponent<?>> components = new HashMap<>();

    private LanguageEntry language;
    private ConfigEntry config;
    private org.apache.logging.log4j.Logger l4jLogger;

    //deprecated
    private Logger logger;
    private RecordEntry record;

    public final void enableModule() throws Exception {
        this.language = this.createLanguage();
        this.config = this.createConfig();
        this.l4jLogger = createL4JLogger();

        this.logger = createLogger();
        this.record = this.useRecord() ? this.createRecord() : new EmptyRecordEntry();

        ModuleServices.onEnable(this);

        this.enable();

        for (var component : this.components.values()) {
            component.enable();
        }
    }

    public final void disableModule() throws Exception {
        for (var component : this.components.values()) {
            component.disable();
        }

        this.disable();
        if (this.record != null) {
            this.record.close();
        }
        ModuleServices.onDisable(this);
    }


    //data support
    @Deprecated
    public abstract Logger createLogger();

    public abstract org.apache.logging.log4j.Logger createL4JLogger();

    public final LanguageEntry createLanguage() {
        return Starlight.instance().language().entry(this.getParent().getId(), this.getId());
    }

    public final ConfigEntry createConfig() {
        return ConfigContainer.getInstance().entry(this.getParent().getId(), Identifiers.external(this.getId()));
    }

    public final RecordEntry createRecord() {
        return RecordService.create(this.getId(), this.getRecordFormat());
    }


    //internal service access
    @Deprecated
    public final Logger getLogger() {
        this.l4jLogger.warn("legacy(java)loggers are no longer supported, please use #getL4JLogger() instead.");
        return this.logger;
    }

    @Deprecated
    public final RecordEntry getRecord() {
        return this.record;
    }

    public final LanguageEntry getLanguage() {
        return this.language;
    }

    public final ConfigEntry getConfig() {
        return this.config;
    }

    public final org.apache.logging.log4j.Logger getL4jLogger() {
        return this.l4jLogger;
    }


    //attributes
    public final SLModule getDescriptor() {
        return this.getClass().getAnnotation(SLModule.class);
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


    //components
    public final Map<Class<? extends ModuleComponent<?>>, ModuleComponent<?>> getComponents() {
        return components;
    }

    public final <I extends ModuleComponent<?>> void getComponent(Class<I> clazz, Consumer<I> consumer) {
        consumer.accept((I) this.components.get(clazz));
    }

    public final <I extends ModuleComponent<?>> I getComponent(Class<I> clazz) {
        return (I) this.components.get(clazz);
    }

    public final Set<AbstractCommand> getCommands() {
        return this.commands;
    }


    //object
    @Override
    public final String toString() {
        return "%s{id=%s version=%s beta=%s, compat-blacklist=%s}".formatted(
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
        throw new CloneNotSupportedException("cannot clone a module instance!");
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

    public final Plugin getOwnerPlugin() {
        return this.getParent().getOwner();
    }

    public final String getDisplayName(MinecraftLocale locale) {
        var lang = Starlight.lang().access(this.getParent().getId());

        if (!lang.handle().hasAny(this.getParent().getId(), "-module-name", this.getId())) {
            return this.getId();
        }

        var displayName = lang.item("-module-name:" + this.getId()).message(locale);
        return "%s{#gray}({#white}%s{#gray})".formatted(getId(), displayName);
    }
}
