package org.tbstcraft.quark.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class KickMessageFetchEvent extends Event{
    public static final HandlerList handlerList = new HandlerList();
    private final String locale;
    private String message;
    private final String playerName;

    public KickMessageFetchEvent(String locale, String message, String playerName) {
        this.locale = locale;
        this.message = message;
        this.playerName = playerName;
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

    public String getPlayerName() {
        return playerName;
    }

    public String getLocale() {
        return locale;
    }
}
