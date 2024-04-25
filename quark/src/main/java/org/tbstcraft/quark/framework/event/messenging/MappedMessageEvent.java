package org.tbstcraft.quark.framework.event.messenging;

import org.tbstcraft.quark.util.container.MapBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class MappedMessageEvent extends MessagingEvent {
    private final Map<String, Object> properties;

    public MappedMessageEvent(String id, Map<String, Object> properties) {
        super(id);
        this.properties = properties;
    }

    public MappedMessageEvent(String id, Consumer<MapBuilder<String, Object>> defaults) {
        this(id, init(defaults));
    }

    private static Map<String, Object> init(Consumer<MapBuilder<String, Object>> consumer) {
        Map<String, Object> args = new HashMap<>();
        consumer.accept(new MapBuilder<>(args));
        return args;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public <T> T getProperty(String name, Class<T> type) {
        return type.cast(this.properties.get(name));
    }
}
