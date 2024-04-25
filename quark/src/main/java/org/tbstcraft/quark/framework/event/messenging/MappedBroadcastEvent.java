package org.tbstcraft.quark.framework.event.messenging;

import org.tbstcraft.quark.framework.event.QuarkEvent;
import org.tbstcraft.quark.util.container.MapBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@QuarkEvent
public final class MappedBroadcastEvent extends MappedMessageEvent {
    public MappedBroadcastEvent(String id, Map<String, Object> properties) {
        super(id, properties);
    }

    public MappedBroadcastEvent(String id, Consumer<MapBuilder<String, Object>> defaults) {
        super(id, defaults);
    }
}
