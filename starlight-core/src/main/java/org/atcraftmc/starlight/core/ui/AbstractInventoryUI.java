package org.atcraftmc.starlight.core.ui;

import org.bukkit.entity.Player;
import org.atcraftmc.starlight.core.ui.element.ElementCallback;
import org.atcraftmc.starlight.core.ui.view.InventoryUIView;
import org.atcraftmc.starlight.core.ui.view.UIViewRenderer;

public abstract class AbstractInventoryUI implements UIViewRenderer {
    protected final int capacity;
    private final ElementCallback open;
    protected TextRenderer title;

    public AbstractInventoryUI(int capacity, TextRenderer titleRenderer, ElementCallback open) {
        this.capacity = capacity;
        this.title = titleRenderer;
        this.open = open;
    }

    @Override
    public InventoryUIView createInventoryUI(Player viewer) {
        var ui = new InventoryUIView(this.capacity, viewer, this.title.applyVirtually(viewer), this.open);
        this.onUIGenerated(ui);
        ui.setData(renderData(ui));
        return ui;
    }

    public void onUIGenerated(InventoryUIView view) {

    }
}
