package org.atcraftmc.starlight.api;

import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;
import org.bukkit.event.HandlerList;

@SLEvent
public final class ChatReportedEvent extends CustomEvent {
    private final String sender;
    private final String content;
    private final String shorted;
    private final String uuid;

    private LanguageItem outcome;

    public ChatReportedEvent(String sender, String content, String shorted, String uuid) {
        this.sender = sender;
        this.content = content;
        this.shorted = shorted;
        this.uuid = uuid;
    }

    public static HandlerList getHandlerList() {
        return getHandlerList(ChatReportedEvent.class);
    }

    public String getContent() {
        return content;
    }

    public String getSender() {
        return sender;
    }

    public String getShorted() {
        return shorted;
    }

    public String getUuid() {
        return uuid;
    }

    public LanguageItem getOutcome() {
        return outcome;
    }

    public void setOutcome(LanguageItem outcome) {
        this.outcome = outcome;
    }
}
