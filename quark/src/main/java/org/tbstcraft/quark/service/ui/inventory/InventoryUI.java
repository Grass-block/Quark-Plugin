package org.tbstcraft.quark.service.ui.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.tbstcraft.quark.service.ui.UI;
import org.tbstcraft.quark.service.ui.UIInstance;
import org.tbstcraft.quark.service.ui.callback.InventoryActionListener;

import java.util.function.Function;

public class InventoryUI extends UI {
    private final InventoryComponent[][] components;
    private final InventoryActionListener[][] listeners;
    private final int columns;
    private Function<Player, String> title = (p) -> "Title";

    public InventoryUI(int columns) {
        this.columns = columns;
        this.components = new InventoryComponent[9][columns];
        this.listeners = new InventoryActionListener[9][columns];
    }

    public void setComponent(int x, int y, InventoryComponent comp) {
        if (x < 0 || x > 8 || y < 0 || y > this.columns) {
            throw new IllegalArgumentException("illegal position[%d,%d]!".formatted(x, y));
        }
        this.components[x][y] = comp;
        comp.onAttached(this, x, y);
    }

    public void setActionListener(int x, int y, InventoryActionListener listener) {
        if (x < 0 || x > 8 || y < 0 || y > this.columns) {
            throw new IllegalArgumentException("illegal position[%d,%d]!".formatted(x, y));
        }
        this.listeners[x][y] = listener;
    }

    public void setTitle(Function<Player, String> title) {
        this.title = title;
    }

    @SuppressWarnings("deprecation")
    public InventoryUIInstance render(Player player) {
        Inventory container = Bukkit.createInventory(null, this.columns * 9, this.title.apply(player));
        InventoryUIInstance instance = new InventoryUIInstance(player, this, container);

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < this.columns; j++) {
                this.components[i][j].onInitialized(instance, player);
            }
        }
        return instance;
    }

    public void open(Player player) {
        this.render(player).open();
    }

    public void setIcon(int x, int y, InventoryIcon icon) {

    }

    public static final class InventoryUIInstance extends UIInstance {
        private final Inventory inventory;
        private final InventoryUI provider;

        public InventoryUIInstance(Player player, InventoryUI provider, Inventory inventory) {
            super(player);
            this.provider = provider;
            this.inventory = inventory;
        }

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (event.getClickedInventory() != this.inventory) {
                return;
            }
            event.setCancelled(true);
            int coordinate = event.getSlot();
            int x = coordinate % 9;
            int y = (int) Math.floor(coordinate / 9f);

            InventoryActionListener handler = this.provider.listeners[x][y];
            if (handler == null) {
                return;
            }
            handler.invoke((Player) event.getWhoClicked(), this, x, y);
        }

        @Override
        public void onClose() {
            this.getPlayer().closeInventory();
        }

        @Override
        public void onOpen() {
            this.getPlayer().openInventory(this.inventory);
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            if (event.getInventory() != this.inventory) {
                return;
            }
            if (this.provider.isCloseable()) {
                return;
            }
            if (this.isActive()) {
                return;
            }
            this.open();
        }

        public Inventory getInventory() {
            return inventory;
        }
    }
}
