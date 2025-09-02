package org.atcraftmc.starlight.core.ui.view;

import org.bukkit.inventory.ItemStack;
import org.atcraftmc.starlight.core.ui.element.ElementCallback;

public final class ViewData {
    private final int size;
    private final ItemStack[] items;
    private final ElementCallback[] callbacks;

    public ViewData(int size) {
        this.size = size;
        this.items = new ItemStack[size];
        this.callbacks = new ElementCallback[size];
    }

    public void setItem(int slot, ItemStack item) {
        this.items[slot] = item;
    }

    public void setCallback(int slot, ElementCallback callback) {
        this.callbacks[slot] = callback;
    }

    public void setElement(int slot, ItemStack item, ElementCallback callback) {
        this.setItem(slot, item);
        this.setCallback(slot, callback);
    }

    public void setItem(int x, int y, ItemStack item) {
        setItem(y * 9 + x, item);
    }

    public void setCallback(int x, int y, ElementCallback callback) {
        setCallback(x * 9 + y, callback);
    }

    public void setElement(int x, int y, ItemStack itemStack, ElementCallback callback) {
        setElement(x * 9 + y, itemStack, callback);
    }

    public ElementCallback[] getCallbacks() {
        return callbacks;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public int getSize() {
        return size;
    }
}
