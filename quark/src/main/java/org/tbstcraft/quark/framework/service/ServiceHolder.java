package org.tbstcraft.quark.framework.service;

public final class ServiceHolder<I> {
    private I t;

    public ServiceHolder(I t) {
        this.t = t;
    }

    public ServiceHolder() {
        this(null);
    }

    public I get() {
        return t;
    }

    public void set(I t) {
        this.t = t;
    }
}
