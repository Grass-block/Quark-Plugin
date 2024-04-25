package org.tbstcraft.quark.framework.event.messenging;

import org.tbstcraft.quark.framework.event.CustomEvent;

import java.util.Objects;

public abstract class MessagingEvent extends CustomEvent {
    private final String id;

    protected MessagingEvent(String id) {
        this.id = id;
    }

    public boolean isRequestedEvent(String id) {
        return Objects.equals(id, this.id);
    }

    public String getId() {
        return id;
    }
}
