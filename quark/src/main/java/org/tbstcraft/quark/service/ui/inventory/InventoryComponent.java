package org.tbstcraft.quark.service.ui.inventory;

import org.bukkit.entity.Player;

public interface InventoryComponent {
    default void onAttached(InventoryUI ui, int x, int y) {
    }

    default void onInitialized(InventoryUI.InventoryUIInstance instance, Player p) {
    }
}
