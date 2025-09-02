package org.atcraftmc.starlight.core.ui.element;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.atcraftmc.starlight.core.ui.view.InventoryUIView;

@FunctionalInterface
public interface ElementCallback {
    void click(InventoryUIView view, Player player, InventoryAction action);
}
