package org.tbstcraft.quark.framework.module;

import org.tbstcraft.quark.data.config.ConfigContainer;
import org.tbstcraft.quark.data.config.ConfigEntry;
import org.tbstcraft.quark.data.language.LanguageContainer;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.framework.packages.IPackage;
import org.tbstcraft.quark.framework.record.RecordEntry;
import org.tbstcraft.quark.framework.record.RecordService;
import org.tbstcraft.quark.util.Identifiers;

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
        return LanguageContainer.INSTANCE.entry(this.parent.getId(), this.getId());
    }

    @Override
    public final ConfigEntry createConfig() {
        return ConfigContainer.getInstance().entry(this.parent.getId(), Identifiers.external(this.getId()));
    }

    @Override
    public final RecordEntry createRecord() {
        return RecordService.create(this.id, this.getRecordFormat());
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
