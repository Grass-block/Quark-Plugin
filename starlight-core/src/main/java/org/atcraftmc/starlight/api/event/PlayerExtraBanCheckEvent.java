package org.atcraftmc.starlight.api.event;

import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;
import org.bukkit.event.HandlerList;

import java.util.Date;
import java.util.UUID;

@SLEvent
public final class PlayerExtraBanCheckEvent extends CustomEvent {
    private final UUID uuid;
    private boolean banned = false;
    private String reason;
    private String source;
    private Date expires;

    public PlayerExtraBanCheckEvent(UUID uuid) {
        this.uuid = uuid;
    }

    public static HandlerList getHandlerList() {
        return getHandlerList(PlayerExtraBanCheckEvent.class);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setBan(String reason, String source, Date expires) {
        this.banned = true;
        this.reason = reason;
        this.source = source;
        this.expires = expires;
    }

    public boolean isBanned() {
        return banned;
    }

    public String getSource() {
        return source;
    }

    public String getReason() {
        return reason;
    }

    public Date getExpires() {
        return expires;
    }
}
