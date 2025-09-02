package org.atcraftmc.starlight.core.ui.element;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.atcraftmc.starlight.core.ui.IconRenderer;
import org.atcraftmc.starlight.core.ui.LoreRenderer;
import org.atcraftmc.starlight.core.ui.TextRenderer;
import org.atcraftmc.starlight.core.ui.view.InventoryUIView;

import java.util.List;

public final class SimpleElement implements UIElement {
    private final IconRenderer item;
    private final TextRenderer title;
    private final LoreRenderer lore;
    private ElementCallback callback;

    public SimpleElement(IconRenderer item, TextRenderer title, LoreRenderer lore) {
        this.item = item;
        this.title = title;
        this.lore = lore;
    }

    public SimpleElement setCallback(ElementCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public ItemStack render(InventoryUIView view) {
        return this.item.apply(view);
    }

    @Override
    public Component title(InventoryUIView view) {
        return this.title.apply(view);
    }

    @Override
    public List<Component> description(InventoryUIView view) {
        return this.lore.apply(view);
    }

    @Override
    public void click(InventoryUIView view, Player player, InventoryAction action) {
        if (this.callback != null) {
            this.callback.click(view, view.getViewer(), action);
        }
    }
}
