package org.atcraftmc.starlight.core.custom.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.atcraftmc.starlight.foundation.platform.APIProfileTest;

public abstract class CustomItem {
    private final String id;
    private final Material icon;
    private final boolean glow;
    private final LanguageItem key;

    public CustomItem() {
        this.id = this.getIdentifier().id();
        this.icon = this.getIdentifier().icon();
        this.glow = this.getIdentifier().enchantGlow();
        this.key = this.getLanguageKey();
    }

    public static Component getDisplayName(ItemStack stack) {
        if (APIProfileTest.isPaperCompat()) {
            return stack.getItemMeta().displayName();
        }

        return Component.text(stack.getItemMeta().getDisplayName());
    }

    public static Component getDisplayName(Material material) {
        return getDisplayName(new ItemStack(material, 1));
    }

    public static void setDisplayName(ItemStack stack, ComponentLike name) {
        if (APIProfileTest.isPaperCompat()) {
            stack.getItemMeta().displayName(name.asComponent());
            return;
        }
        stack.getItemMeta().setDisplayName(LegacyComponentSerializer.legacySection().serialize(name.asComponent()));
    }

    public QuarkItem getIdentifier() {
        return this.getClass().getAnnotation(QuarkItem.class);
    }

    public ItemStack create(int amount, MinecraftLocale locale) {
        ItemStack stack = new ItemStack(this.icon, amount);
        setDisplayName(stack, this.renderDisplayName(stack, locale));
        return stack;
    }

    public abstract ComponentLike renderDisplayName(ItemStack stack, MinecraftLocale locale);

    public abstract LanguageItem getLanguageKey();
}
