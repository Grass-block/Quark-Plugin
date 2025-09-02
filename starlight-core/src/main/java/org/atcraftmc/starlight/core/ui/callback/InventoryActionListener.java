package org.atcraftmc.starlight.core.ui.callback;

import org.bukkit.entity.Player;
import org.atcraftmc.starlight.core.ui.inventory.InventoryUI;

public interface InventoryActionListener {
    void invoke(Player p, InventoryUI.InventoryUIInstance ui, int x, int y);
}
