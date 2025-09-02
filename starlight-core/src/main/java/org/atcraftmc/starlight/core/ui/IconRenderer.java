package org.atcraftmc.starlight.core.ui;

import org.bukkit.inventory.ItemStack;
import org.atcraftmc.starlight.core.ui.view.InventoryUIView;

import java.util.function.Function;

public interface IconRenderer extends Function<InventoryUIView, ItemStack> {
    static IconRenderer fixed(ItemStack stack) {
        return (v) -> stack;
    }
}
