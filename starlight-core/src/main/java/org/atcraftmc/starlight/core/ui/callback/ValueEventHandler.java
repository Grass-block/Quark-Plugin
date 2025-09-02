package org.atcraftmc.starlight.core.ui.callback;

import org.bukkit.entity.Player;
import org.atcraftmc.starlight.core.ui.UIInstance;

public interface ValueEventHandler {
    void invoke(Player player, UIInstance ui, int value);
}
