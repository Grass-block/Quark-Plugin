package org.tbstcraft.quark.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class PackContainer<I extends ConfigurationPack> {
    protected final Map<String, I> packs = new HashMap<>();

    public void register(I pack) {
        this.packs.put(pack.getRegisterId(), pack);
        this.inject(pack);
    }

    public void unregister(I pack) {
        this.packs.remove(pack.toString());
    }

    public Collection<I> getPacks() {
        return packs.values();
    }

    public I getPack(String id) {
        return this.packs.get(id);
    }

    public Map<String, I> getPackStorage() {
        return this.packs;
    }

    public abstract void inject(I pack);

    public void refresh(boolean b) {
        for (var pack : this.packs.values()) {
            this.inject(pack);
        }
    }
}
