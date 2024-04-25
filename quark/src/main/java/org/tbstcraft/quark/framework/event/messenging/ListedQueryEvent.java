package org.tbstcraft.quark.framework.event.messenging;

import org.tbstcraft.quark.framework.event.QuarkEvent;
import org.tbstcraft.quark.util.container.ListBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@QuarkEvent
public final class ListedQueryEvent extends ListedMessageEvent {
    public ListedQueryEvent(String id) {
        super(id, new ArrayList<>(16));
    }

    public ListedQueryEvent(String id, Consumer<ListBuilder<Object>> defaults) {
        super(id, defaults);
    }

    public ListedQueryEvent setArgument(int position, Object arg) {
        this.getArgs().add(position, arg);
        return this;
    }

    public ListedQueryEvent addArgument(Object arg) {
        this.getArgs().add(arg);
        return this;
    }
}
