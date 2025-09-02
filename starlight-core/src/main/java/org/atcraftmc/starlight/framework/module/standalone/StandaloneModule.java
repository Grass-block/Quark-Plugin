package org.atcraftmc.starlight.framework.module.standalone;

import org.atcraftmc.starlight.framework.module.AbstractModule;

import java.util.logging.Logger;

public abstract class StandaloneModule extends AbstractModule {
    private final StandaloneModuleProvider provider;

    protected StandaloneModule(StandaloneModuleProvider provider) {
        this.provider = provider;
    }

    @Override
    public Logger createLogger() {
        return this.provider.getLogger();
    }

    @Override
    public org.apache.logging.log4j.Logger createL4JLogger() {
        return this.provider.getLog4JLogger();
    }

    @Override
    public String getId() {
        return this.provider.getName().toLowerCase();
    }

    public StandaloneModuleProvider getParent() {
        return this.provider;
    }
}
