package org.atcraftmc.starlight.api.event;

import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;
import org.bukkit.BanList;
import org.bukkit.event.HandlerList;

import java.util.Date;

@SLEvent
public final class BanMessageFetchEvent extends CustomEvent {
    private final BanList.Type type;
    private final String target;
    private final String source;
    private final String reason;
    private final Date expires;
    private final MinecraftLocale locale;
    private String resultMessage;

    public BanMessageFetchEvent(BanList.Type type, MinecraftLocale locale, String target, String source, String reason, Date expires) {
        this.type = type;
        this.locale = locale;
        this.target = target;
        this.source = source;
        this.reason = reason;
        this.expires = expires;
    }

    public static HandlerList getHandlerList() {
        return getHandlerList(BanMessageFetchEvent.class);
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getSource() {
        return source;
    }

    public Date getExpires() {
        return expires;
    }

    public String getReason() {
        return reason;
    }

    public String getTarget() {
        return target;
    }

    public BanList.Type getType() {
        return type;
    }

    public MinecraftLocale getLocale() {
        return locale;
    }
}
