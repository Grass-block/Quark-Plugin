package org.atcraftmc.starlight.core.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;

public abstract class UIInstance implements Listener {
    private final Player player;
    private boolean active = false;

    public UIInstance(Player player) {
        BukkitUtil.registerEventListener(this);
        this.player = player;
    }

    public final void open() {
        this.active = true;
        this.onOpen();
    }

    public final void close() {
        this.active = false;
        this.onClose();
    }

    public abstract void onClose();

    public abstract void onOpen();

    public void destroy() {
        BukkitUtil.unregisterEventListener(this);
        this.close();
    }


    public boolean isActive() {
        return active;
    }

    protected Player getPlayer() {
        return player;
    }
}
