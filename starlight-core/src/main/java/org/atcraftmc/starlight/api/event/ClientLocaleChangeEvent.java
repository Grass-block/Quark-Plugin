package org.atcraftmc.starlight.api.event;

import org.atcraftmc.qlib.language.MinecraftLocale;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;

@SLEvent(async = false)
public final class ClientLocaleChangeEvent extends CustomEvent {
    private final Player player;
    private final MinecraftLocale locale;

    public ClientLocaleChangeEvent(Player player, MinecraftLocale locale) {
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

    public MinecraftLocale getLocale() {
        return this.locale;
    }
}
