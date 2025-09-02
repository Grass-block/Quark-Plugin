package org.atcraftmc.starlight.core.ui.view;

import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.core.ui.element.ElementCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

public final class InventoryUIView implements Listener {
    public static final InventoryUIView DUMMY = new InventoryUIView(54, null, Component.text("_dummy"));

    public static final Logger LOGGER = LogManager.getLogger("QuarkCore/UIView");
    private final Map<String, Object> customData = new HashMap<>();
    private final Player viewer;
    private final Inventory inventory;
    private final ElementCallback open;
    private ViewData viewData;

    public InventoryUIView(int nSlot, Player viewer, Component title, ElementCallback openOperation) {
        this.viewer = viewer;
        this.open = openOperation;
        this.inventory = create(nSlot, title);
    }

    public InventoryUIView(int nSlot, Player viewer, Component title) {
        this.viewer = viewer;
        this.open = null;
        this.inventory = create(nSlot, title);
    }

    private static Inventory create(int nSlots, Component title) {
        try {
            return Bukkit.createInventory(null, nSlots, title);
        } catch (NoSuchElementException e) {
            return Bukkit.createInventory(null, nSlots, ComponentSerializer.legacy(title));
        }
    }

    public InventoryUIView setData(ViewData viewData) {
        if (viewData.getSize() != this.inventory.getSize()) {
            throw new IllegalArgumentException("View data has wrong size(%s), expected %s".formatted(
                    viewData.getSize(),
                    this.inventory.getSize()
            ));
        }

        this.viewData = viewData;
        this.inventory.clear();
        this.inventory.setContents(viewData.getItems());

        return this;
    }

    public void setCustomData(String key, Object value) {
        this.customData.put(key, value);
    }

    public <D> D getCustomData(String key, Class<D> type) {
        return type.cast(this.customData.get(key));
    }

    public <D> D getCustomData(String key, Class<D> type, D defaultValue) {
        if (!this.customData.containsKey(key)) {
            this.customData.put(key, defaultValue);
            return defaultValue;
        }

        return type.cast(this.customData.get(key));
    }

    public <D> D getCustomData(String key, Class<D> type, Supplier<D> defaultValue) {
        if (!this.customData.containsKey(key)) {
            this.customData.put(key, defaultValue.get());
        }

        return type.cast(this.customData.get(key));
    }


    public void open() {
        BukkitUtil.registerEventListener(this);
        if (this.open != null) {
            this.open.click(this, this.viewer, InventoryAction.UNKNOWN);
        }
        this.viewer.openInventory(this.inventory);
    }

    public void close() {
        if (this.viewer.getOpenInventory().getTopInventory() != this.inventory) {
            return;
        }
        this.viewer.closeInventory();
        BukkitUtil.unregisterEventListener(this);
    }

    public Player getViewer() {
        return viewer;
    }

    public ViewData getDOM() {
        return viewData;
    }

    public Map<String, Object> getCustomData() {
        return customData;
    }

    public Inventory getHandle() {
        return inventory;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        BukkitUtil.unregisterEventListener(this);
    }

    @EventHandler
    public void onInventoryClicked(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getClickedInventory() != this.inventory) {
            return;
        }

        var cb = this.viewData.getCallbacks()[event.getSlot()];

        if (cb == null) {
            return;
        }

        try {
            cb.click(this, this.viewer, event.getAction());
        } catch (Exception e) {
            LOGGER.error("An exception occurred while executing event on {} for {}", event.getSlot(), this.viewer);
            LOGGER.catching(e);
        }
    }
}
