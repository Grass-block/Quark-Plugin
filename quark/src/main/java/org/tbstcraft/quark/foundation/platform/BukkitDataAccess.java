package org.tbstcraft.quark.foundation.platform;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public interface BukkitDataAccess {

    static <I extends BlockData> I blockData(Block block, Class<I> type) {
        return type.cast(block.getBlockData());
    }

    static <I extends BlockState> I blockState(Block block, Class<I> type) {
        return type.cast(block.getState());
    }

    static <I extends BlockData> I blockDataAccess(Block block, Class<I> type, Consumer<I> accepter) {
        var data = blockData(block, type);
        accepter.accept(data);
        block.setBlockData(data);
        return data;
    }

    static void itemMeta(ItemStack item, Consumer<ItemMeta> accepter) {
        var itemMeta = item.getItemMeta();
        accepter.accept(itemMeta);
        item.setItemMeta(itemMeta);
    }
}
