package org.atcraftmc.starlight.api.event;

import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.Date;

@SLEvent(async = false)
public final class KickMessageFetchEvent extends CustomEvent {
    private final Player player;
    private final String reason;
    private final MinecraftLocale locale;
    private String resultMessage;

    public KickMessageFetchEvent(Player player, String reason, MinecraftLocale locale) {
        this.player = player;
        this.reason = reason;
        this.locale = locale;
    }

    public static HandlerList getHandlerList() {
        return getHandlerList(KickMessageFetchEvent.class);
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getReason() {
        return reason;
    }

    public MinecraftLocale getLocale() {
        return locale;
    }

    public Player getPlayer() {
        return player;
    }
}
