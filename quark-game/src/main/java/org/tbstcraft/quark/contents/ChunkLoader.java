package org.tbstcraft.quark.contents;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.framework.customcontent.block.CustomBlock;
import org.tbstcraft.quark.framework.customcontent.block.QuarkBlock;
import org.tbstcraft.quark.util.api.BukkitUtil;

public class ChunkLoader {


    public static ItemStack createChunkLoaderItem(int amount) {
        ItemStack stack = new ItemStack(Material.BEACON, amount);
        BukkitUtil.setItemUsage(stack, "quark::chunk_loader");
        return stack;
    }


    @QuarkBlock(id = "quark_game:chunk_loader", icon = Material.BEACON)
    public static final class ChunkLoaderBlock extends CustomBlock {

        public ChunkLoaderBlock() {
            super(null);
        }

        @Override
        public String getDisplayName(CommandSender target) {
            return null;
        }
    }
}
