package org.tbstcraft.quark.framework.event;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.event.HandlerList;

@QuarkEvent
public class BanMessageFetchEvent extends CustomEvent {
    private final BanEntry entry;
    private final String locale;
    private final BanList.Type type;
    private String message;

    public BanMessageFetchEvent(BanEntry entry, BanList.Type type, String locale, String message) {
        this.entry = entry;
        this.locale = locale;
        this.message = message;
        this.type = type;
    }

    @SuppressWarnings({"unused"})
    public static HandlerList getHandlerList() {
        return getHandlerList(BanMessageFetchEvent.class);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BanEntry getEntry() {
        return entry;
    }

    public String getLocale() {
        return locale;
    }

    public BanList.Type getType() {
        return type;
    }
}
