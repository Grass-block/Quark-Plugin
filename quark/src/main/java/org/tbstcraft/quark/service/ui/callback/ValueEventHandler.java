package org.tbstcraft.quark.service.ui.callback;

import org.bukkit.entity.Player;
import org.tbstcraft.quark.service.ui.UIInstance;

public interface ValueEventHandler {
    void invoke(Player player, UIInstance ui, int value);
}
