package org.tbstcraft.quark.service.ui.inventory;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.service.ui.callback.InventoryActionListener;

public class Button implements InventoryComponent, InventoryActionListener {
    private final InventoryIcon icon;
    private final InventoryActionListener handler;
    private final Sound sound;
    private int x = -999;
    private int y = -999;

    public Button(InventoryIcon icon, Sound sound, InventoryActionListener handler) {
        this.icon = icon;
        this.handler = handler;
        this.sound = sound;
    }

    public Button(InventoryIcon icon, InventoryActionListener handler) {
        this(icon, Sound.UI_BUTTON_CLICK, handler);
    }


    @Override
    public void invoke(Player p, InventoryUI.InventoryUIInstance ui, int x, int y) {
        p.playSound(p.getLocation(), this.sound, 1, 0);
        this.handler.invoke(p, ui, x, y);
    }

    @Override
    public void onAttached(InventoryUI ui, int x, int y) {
        this.x = x;
        this.y = y;
        ui.setActionListener(x, y, this);
    }

    @Override
    public void onInitialized(InventoryUI.InventoryUIInstance instance, Player p) {
        instance.getInventory().setItem(this.y * 9 + this.x, this.icon.render(p));
    }
}
