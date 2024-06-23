package org.tbstcraft.quark.internal.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class UI implements Listener {
    private boolean closeable;

    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
    }

    public boolean isCloseable() {
        return closeable;
    }

    public abstract UIInstance render(Player player);
}
