package org.atcraftmc.starlight.api.customization;

import org.atcraftmc.qlib.bukkit.ComponentSerializer;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.qlib.texts.ComponentBlock;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.custom.CustomMeta;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public abstract class CustomItem {
    public final String id;

    protected CustomItem(String id) {
        this.id = id;
    }

    public final String getId() {
        return id;
    }

    public abstract LanguageItem getDisplayName(ItemStack stack);

    public abstract LanguageItem getDescription(ItemStack stack);

    public abstract Material getActualBlock();

    public final ItemStack createItem(int amount) {
        var stack = new ItemStack(this.getActualBlock(), amount);
        CustomMeta.setItemPDCIdentifier(stack, this.id);
        render(stack, LocaleService.locale(Bukkit.getConsoleSender()));
        return stack;
    }

    public void onItemInteractAtBlock(Player player, ItemStack stack, Block target, Action action) {
    }

    public void onItemInteractAtAir(Player player, ItemStack stack, Action action) {
    }

    public final void render(ItemStack stack, MinecraftLocale locale) {
        var n = this.getDisplayName(stack).component(locale).asComponent();
        var c = this.getDescription(stack).listComponent(locale);

        var m = stack.getItemMeta();

        try {
            m.displayName(n);
            m.lore(c.stream().map(ComponentBlock::asComponent).collect(Collectors.toList()));
        } catch (Throwable e) {
            m.setDisplayName(ComponentSerializer.legacy(n));
            m.setLore(c.stream().map(ComponentSerializer::legacy).collect(Collectors.toList()));
        }

        stack.setItemMeta(m);
    }

    public void onItemPick(Player player, ItemStack stack) {
    }
}
