package org.tbstcraft.quark.service.ui.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

@SuppressWarnings("deprecation")
public final class InventoryIcon {
    private final Function<Player, String> displayName;
    private final Material icon;
    private final int count;

    public InventoryIcon(Function<Player, String> displayName, Material icon, int count) {
        this.displayName = displayName;
        this.icon = icon;
        this.count = count;
    }

    public InventoryIcon(Function<Player, String> displayName, Material icon) {
        this(displayName, icon, 1);
    }

    public InventoryIcon(ItemStack s1) {
        this((p)->s1.getItemMeta().getDisplayName(), s1.getType(), s1.getAmount());
    }

    public ItemStack render(Player player) {
        ItemStack stack = new ItemStack(this.icon, this.count);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(this.displayName.apply(player));
        stack.setItemMeta(meta);
        return stack;
    }
}
