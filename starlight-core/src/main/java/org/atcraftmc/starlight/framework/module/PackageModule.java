package org.atcraftmc.starlight.framework.module;

import org.apache.logging.log4j.LogManager;
import org.atcraftmc.starlight.framework.packages.IPackage;

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
    public final Logger createLogger() {
        return Logger.getLogger(this.parent.getLoggerName() + "/" + this.getClass().getSimpleName());
    }

    @Override
    public org.apache.logging.log4j.Logger createL4JLogger() {
        return LogManager.getLogger(this.parent.getLoggerName() + "/" + this.getClass().getSimpleName());
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
