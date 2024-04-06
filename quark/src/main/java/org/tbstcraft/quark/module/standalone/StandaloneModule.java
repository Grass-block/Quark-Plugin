package org.tbstcraft.quark.module.standalone;

import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.config.Configuration;
import org.tbstcraft.quark.config.Language;
import org.tbstcraft.quark.config.LanguageEntry;
import org.tbstcraft.quark.module.AbstractModule;
import org.tbstcraft.quark.service.record.RecordEntry;
import org.tbstcraft.quark.service.record.RecordService;
import org.tbstcraft.quark.util.FilePath;

import java.util.logging.Logger;

public abstract class StandaloneModule extends AbstractModule {
    private final StandaloneModuleProvider provider;
    private final RecordService recordService;

    protected StandaloneModule(StandaloneModuleProvider provider) {
        this.provider = provider;
        this.recordService = RecordService.create(FilePath.pluginFolder(this.provider.getName()) + "/records");
    }

    @Override
    public LanguageEntry createLanguage() {
        return Language.create(this.provider, this.getId()).createEntry(this.getId());
    }

    @Override
    public RecordEntry createRecord() {
        return this.recordService.createEntry(this.getId(), this.getId(), this.getRecordFormat());
    }

    @Override
    public ConfigurationSection createConfig() {
        return new Configuration(this.provider, this.getId()).getConfig(this.getId());
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
