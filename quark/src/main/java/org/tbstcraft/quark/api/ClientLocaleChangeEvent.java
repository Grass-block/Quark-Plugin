package org.tbstcraft.quark.api;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.tbstcraft.quark.framework.event.CustomEvent;
import org.tbstcraft.quark.framework.event.QuarkEvent;

import java.util.Locale;

@QuarkEvent(async = false)
public final class ClientLocaleChangeEvent extends CustomEvent {
    private final Player player;
    private final Locale locale;

    public ClientLocaleChangeEvent(Player player, Locale locale) {
        this.player = player;
        this.locale = locale;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return getHandlerList(ClientLocaleChangeEvent.class);
    }

    public Player getPlayer() {
        return this.player;
    }

    public Locale getLocale() {
        return this.locale;
    }
}
