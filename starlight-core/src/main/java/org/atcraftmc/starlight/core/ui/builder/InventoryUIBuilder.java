package org.atcraftmc.starlight.core.ui.builder;

import org.atcraftmc.starlight.core.ui.inventory.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.atcraftmc.starlight.core.ui.callback.InventoryActionListener;
import org.atcraftmc.starlight.core.ui.callback.ValueEventHandler;

import java.util.function.Function;

public class InventoryUIBuilder extends UIBuilder {
    private final InventoryUI ui;

    public InventoryUIBuilder(int slots) {
        this.ui = new InventoryUI(slots);
    }

    //attribute
    public InventoryUIBuilder title(Function<Player, String> title) {
        this.ui.setTitle(title);
        return this;
    }

    public InventoryUIBuilder closeable(boolean closeable) {
        this.ui.setCloseable(closeable);
        return this;
    }

    //base
    public InventoryUIBuilder component(int x, int y, InventoryComponent component) {
        this.ui.setComponent(x, y, component);
        return this;
    }

    public InventoryUIBuilder listener(int x, int y, InventoryActionListener listener) {
        this.ui.setActionListener(x, y, listener);
        return this;
    }

    //component
    public InventoryUIBuilder button(int x, int y, InventoryIcon icon, Sound sound, InventoryActionListener listener) {
        return component(x, y, new Button(icon, sound, listener));
    }

    public InventoryUIBuilder button(int x, int y, InventoryIcon icon, InventoryActionListener listener) {
        return component(x, y, new Button(icon, listener));
    }

    @Deprecated
    public InventoryUIBuilder close(int x, int y) {
        return close(x, y, new InventoryIcon((p) -> "close", Material.RED_CONCRETE));
    }

    public InventoryUIBuilder close(int x, int y, InventoryIcon icon) {
        return button(x, y, icon, (p, ui, xx, yy) -> ui.destroy());
    }

    public InventoryUIBuilder command(int x, int y, InventoryIcon icon, String command) {
        return button(x, y, icon, (p, ui, xx, yy) -> Bukkit.getServer().dispatchCommand(p, command));
    }


    public InventoryUIBuilder valueChangerH4(int x, int y, int initial, int min, int max, ValueEventHandler handler) {
        new ValueChanger(false, x, y, min, max, initial, 4, handler);
        return this;
    }

    public InventoryUI build() {
        return this.ui;
    }


    //todo: /menu
    //todo: popup/conversation(preset)
}
