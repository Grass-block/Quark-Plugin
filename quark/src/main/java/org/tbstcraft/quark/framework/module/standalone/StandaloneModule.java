package org.tbstcraft.quark.framework.module.standalone;

import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.framework.data.config.Configuration;
import org.tbstcraft.quark.framework.data.config.Language;
import org.tbstcraft.quark.framework.data.language.LanguageEntry;
import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.internal.record.RecordEntry;
import org.tbstcraft.quark.internal.record.RecordService;
import org.tbstcraft.quark.util.Identifiers;

import java.util.logging.Logger;

public abstract class StandaloneModule extends AbstractModule {
    private final StandaloneModuleProvider provider;
    private final RecordService recordService;

    protected StandaloneModule(StandaloneModuleProvider provider) {
        this.provider = provider;
        this.recordService = RecordService.create();
    }

    @Override
    public LanguageEntry createLanguage() {
        return Language.create(this.provider, this.getId()).entry(this.getId());
    }

    @Override
    public RecordEntry createRecord() {
        return this.recordService.createEntry(this.getId(), this.getRecordFormat());
    }

    @Override
    public ConfigurationSection createConfig() {
        return new Configuration(this.provider, Identifiers.external(this.getId())).getConfig(Identifiers.external(this.getId()));
    }

    @Override
    public Logger createLogger() {
        return this.provider.getLogger();
    }

    @Override
    public String getId() {
        return this.provider.getName().toLowerCase();
    }

    public StandaloneModuleProvider getParent() {
        return this.provider;
    }
}
