package org.tbstcraft.quark.internal.ui.callback;

import org.bukkit.entity.Player;
import org.tbstcraft.quark.internal.ui.UIInstance;

public interface ValueEventHandler {
    void invoke(Player player, UIInstance ui, int value);
}
