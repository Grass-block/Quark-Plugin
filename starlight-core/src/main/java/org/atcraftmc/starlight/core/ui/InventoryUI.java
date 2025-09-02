package org.atcraftmc.starlight.core.ui;

import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO1;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.core.ui.element.ElementCallback;
import org.atcraftmc.starlight.core.ui.element.UIElement;
import org.atcraftmc.starlight.core.ui.element.UIElementRenderer;
import org.atcraftmc.starlight.core.ui.view.InventoryUIView;
import org.atcraftmc.starlight.core.ui.view.ViewData;

import java.util.List;


public class InventoryUI extends AbstractInventoryUI {

    @SuppressWarnings("Convert2MethodRef")
    public static final MethodHandleO1<ItemMeta, Component> SET_DISPLAY_NAME = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> ItemMeta.class.getMethod("displayName", Component.class), (m, c) -> m.displayName(c));
        ctx.attempt(
                () -> ItemMeta.class.getMethod("setDisplayNameComponent", BaseComponent[].class),
                (m, c) -> m.setDisplayNameComponent(ComponentSerializer.bungee(c))
        );
        ctx.dummy((m, c) -> m.setDisplayName(ComponentSerializer.legacy(c)));
    });

    @SuppressWarnings("Convert2MethodRef")
    public static final MethodHandleO1<ItemMeta, List<Component>> SET_LORE = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> ItemMeta.class.getMethod("lore", List.class), (m, c) -> m.lore(c));
        ctx.attempt(
                () -> ItemMeta.class.getMethod("setLoreComponents", List.class),
                (m, c) -> m.setLoreComponents(c.stream().map(ComponentSerializer::bungee).toList())
        );
        ctx.dummy((m, c) -> m.setLore(c.stream().map(ComponentSerializer::legacy).toList()));
    });

    private final UIElementRenderer[] renderers;
    private final ElementCallback[] callbacks;

    public InventoryUI(int capacity, TextRenderer titleRenderer, ElementCallback open) {
        super(capacity, titleRenderer, open);
        this.renderers = new UIElementRenderer[capacity];
        this.callbacks = new ElementCallback[capacity];
    }

    public InventoryUI(int capacity, TextRenderer titleRenderer) {
        super(capacity, titleRenderer, null);
        this.renderers = new UIElementRenderer[capacity];
        this.callbacks = new ElementCallback[capacity];
    }

    @Override
    public ViewData renderData(InventoryUIView view) {
        var data = new ViewData(this.capacity);

        for (var i = 0; i < this.capacity; i++) {
            if (this.renderers[i] != null) {
                data.setItem(i, makeIcon(view, i));
            }
            if (this.callbacks[i] != null) {
                data.setCallback(i, this.callbacks[i]);
            }
        }

        return data;
    }

    public ItemStack makeIcon(InventoryUIView view, int position) {
        if (this.renderers[position] == null) {
            return null;
        }

        var renderer = this.renderers[position];
        var icon = renderer.render(view);
        var meta = icon.getItemMeta();

        SET_DISPLAY_NAME.invoke(meta, renderer.title(view));
        SET_LORE.invoke(meta, renderer.description(view));

        icon.setItemMeta(meta);

        return icon;
    }


    public void setRenderer(int position, UIElementRenderer renderer) {
        this.renderers[position] = renderer;
    }

    public void setCallback(int position, ElementCallback callback) {
        this.callbacks[position] = callback;
    }

    public void setElement(int position, UIElement element) {
        setCallback(position, element);
        setRenderer(position, element);
    }

    public InventoryUI setElement(int x, int y, UIElement element) {
        setElement(y * 9 + x, element);
        return this;
    }

    public void title(TextRenderer data) {
        this.title = data;
    }
}
