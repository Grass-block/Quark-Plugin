package org.tbstcraft.quark.module;

import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.config.LanguageEntry;
import org.tbstcraft.quark.packages.IPackage;
import org.tbstcraft.quark.service.record.RecordEntry;
import org.tbstcraft.quark.service.record.RecordService;

import java.util.logging.Logger;

public abstract class PackageModule extends AbstractModule {
    public Logger logger;
    private IPackage parent;
    private String id;

    public final void init(String id, IPackage parent) {
        this.id = id;
        this.parent = parent;
    }

    //lifecycle
    @Override
    public final LanguageEntry createLanguage() {
        return this.parent.getLanguageFile().createEntry(this.getId());
    }

    @Override
    public final ConfigurationSection createConfig() {
        return this.parent.getConfigFile().getConfig(this.getId());
    }

    @Override
    public final RecordEntry createRecord() {
        return RecordService.create(this.getParent().getId(), this.id, this.getRecordFormat());
    }

    @Override
    public final Logger createLogger() {
        return Logger.getLogger(this.parent.getLoggerName() + "/" + this.getClass().getSimpleName());
    }


    //attribute
    @Override
    public final String getId() {
        return this.id;
    }

    @Override
    public final String getFullId() {
        return this.getParent().getId() + ":" + this.getId();
    }

    @Override
    public final IPackage getParent() {
        return this.parent;
    }
}
