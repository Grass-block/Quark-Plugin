package org.tbstcraft.quark.service.ui.callback;

import org.bukkit.entity.Player;
import org.tbstcraft.quark.service.ui.inventory.InventoryUI;

public interface InventoryActionListener {
    void invoke(Player p, InventoryUI.InventoryUIInstance ui, int x, int y);
}
