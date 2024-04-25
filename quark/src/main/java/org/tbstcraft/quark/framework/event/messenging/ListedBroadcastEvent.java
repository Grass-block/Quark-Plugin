package org.tbstcraft.quark.framework.event.messenging;

import org.tbstcraft.quark.framework.event.QuarkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@QuarkEvent
public final class ListedBroadcastEvent extends ListedMessageEvent {
    public ListedBroadcastEvent(String id, Object... arguments) {
        super(id, List.of(arguments));
    }

    public ListedBroadcastEvent(String id, List<Object> arguments) {
        super(id, arguments);
    }

    public ListedBroadcastEvent(String id, Set<Object> arguments) {
        super(id, arguments.stream().toList());
    }

    public ListedBroadcastEvent(String id, Consumer<List<Object>> defaults) {
        super(id, defaults);
    }
}
