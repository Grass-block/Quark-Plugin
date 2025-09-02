package org.atcraftmc.starlight.core.ui.providing;

import org.bukkit.entity.Player;
import org.atcraftmc.starlight.core.ui.AbstractInventoryUI;
import org.atcraftmc.starlight.core.ui.view.InventoryUIView;
import org.atcraftmc.starlight.core.ui.view.ViewData;

public interface GUIProvider<I extends AbstractInventoryUI> {

    I create();

    default InventoryUIView initializeView(I builder, Player viewer, Object... args) {
        return builder.createInventoryUI(viewer);
    }

    void render(I ui, InventoryUIView view, Object... args);

    default ViewData renderData(InventoryUIView view, Object... args) {
        var ui = create();
        render(ui, view, args);
        return ui.renderData(view);
    }

    default void rebuildView(InventoryUIView view, Object... args) {
        var ui = create();
        render(ui, view, args);
        view.setData(ui.renderData(view));
    }

    default void open(Player player, Object... args) {
        var ui = create();
        var view = initializeView(ui, player, args);
        render(ui, view, args);
        view.setData(ui.renderData(view));
        view.open();
    }
}
