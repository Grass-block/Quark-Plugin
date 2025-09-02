package org.atcraftmc.starlight.core.ui.view;

import org.bukkit.entity.Player;

public interface UIViewRenderer {
    ViewData renderData(InventoryUIView viewer);

    InventoryUIView createInventoryUI(Player viewer);
}
