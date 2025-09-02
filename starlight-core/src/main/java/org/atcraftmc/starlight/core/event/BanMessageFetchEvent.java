package org.atcraftmc.starlight.core.event;

import org.bukkit.BanList;
import org.bukkit.event.HandlerList;

import java.util.Date;

@SLEvent
public class BanMessageFetchEvent extends CustomEvent {
    private final BanList.Type type;
    private String message;
    private final Date expiration;
    private final String target;
    private final String source;
    private final String locale;

    public BanMessageFetchEvent(BanList.Type type, String message, Date expiration, String target, String source, String locale) {
        this.type = type;
        this.message = message;
        this.expiration = expiration;
        this.target = target;
        this.source = source;
        this.locale = locale;
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

    public Date getExpiration() {
        return expiration;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getLocale() {
        return locale;
    }

    public BanList.Type getType() {
        return type;
    }
}
