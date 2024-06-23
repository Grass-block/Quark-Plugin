package org.tbstcraft.quark.internal.ui.callback;

import org.bukkit.entity.Player;
import org.tbstcraft.quark.internal.ui.inventory.InventoryUI;

public interface InventoryActionListener {
    void invoke(Player p, InventoryUI.InventoryUIInstance ui, int x, int y);
}
