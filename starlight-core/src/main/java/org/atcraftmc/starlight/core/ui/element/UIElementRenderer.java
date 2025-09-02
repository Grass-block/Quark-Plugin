package org.atcraftmc.starlight.core.ui.element;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.atcraftmc.starlight.core.ui.view.InventoryUIView;

import java.util.List;

public interface UIElementRenderer {
    ItemStack render(InventoryUIView viewer);

    Component title(InventoryUIView viewer);

    List<Component> description(InventoryUIView viewer);
}
