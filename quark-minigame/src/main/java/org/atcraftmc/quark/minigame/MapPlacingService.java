package org.atcraftmc.quark.minigame;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.tbstcraft.quark.foundation.platform.MaterialMapping;

public class MapPlacingService {




    private static void setBlock(int x, int y, int z, int blockId, int blockData) {
        World world = Bukkit.getWorld("world"); // 获取世界对象
        Block block = world.getBlockAt(x, y, z); // 获取方块对象
        block.setType(MaterialMapping.get(blockId)); // 设置方块类型
       // block.getClass().getMethod("setData",int.class) // 设置方块数据值
    }
}
