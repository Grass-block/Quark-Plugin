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
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public abstract class CustomBlock {
    public final String id;

    protected CustomBlock(String id) {
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

    public final void render(ItemStack stack, MinecraftLocale locale) {
        var n = this.getDisplayName(stack).component(locale).asComponent();
        var c = this.getDescription(stack).listComponent(locale);

        var m = stack.getItemMeta();

        try {
            m.displayName(n);
            m.lore(c.stream().map(ComponentBlock::asComponent).collect(Collectors.toList()));
        } catch (Exception e) {
            m.setDisplayName(ComponentSerializer.legacy(n));
            m.setLore(c.stream().map(ComponentSerializer::legacy).collect(Collectors.toList()));
        }

        stack.setItemMeta(m);
    }

    public void onItemPick(Player player, ItemStack stack) {
    }

    public void onPlaced(Player player, Block blockPlaced, ItemStack item) {
    }

    public ItemStack onBlockBreak(@NotNull Player player, Block state) {
        return createItem(1);
    }

    public boolean test(Block b) {
        if (!(b.getState() instanceof PersistentDataHolder state)) {
            return false;
        }

        if (!CustomMeta.hasPDCIdentifier(state)) {
            return false;
        }

        return CustomMeta.getPDCIdentifier(state).equals(this.id);
    }
}
