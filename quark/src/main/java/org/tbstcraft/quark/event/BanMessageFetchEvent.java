package org.tbstcraft.quark.event;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BanMessageFetchEvent extends Event {
    public static final HandlerList handlerList = new HandlerList();
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


    @SuppressWarnings({"SameReturnValue", "unused"})
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
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
