package org.tbstcraft.quark.framework.event.messenging;

import org.tbstcraft.quark.framework.event.QuarkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@QuarkEvent
public final class MappedQueryEvent extends MappedMessageEvent {
    public MappedQueryEvent(String id) {
        super(id, new HashMap<>());
    }

    public MappedQueryEvent(String id, Consumer<Map<String, Object>> defaults) {
        super(id, defaults);
    }

    public MappedQueryEvent setProperty(String name, Object obj) {
        this.getProperties().put(name, obj);
        return this;
    }
}
