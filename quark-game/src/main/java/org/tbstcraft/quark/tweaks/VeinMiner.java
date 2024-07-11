package org.tbstcraft.quark.tweaks;

import me.gb2022.commons.reflect.Inject;
import org.bukkit.Axis;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.data.PlaceHolderStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import me.gb2022.commons.reflect.AutoRegister;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(id = "vein_miner")
public final class VeinMiner extends PackageModule {
    public static final int MAX_DEEP = 64;
    private final Set<String> breakingSession = new HashSet<>();

    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PlaceHolderStorage.get(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, HashSet.class, (s) -> s.add(this.tip));
    }

    @Override
    public void disable(){
        PlaceHolderStorage.get(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, HashSet.class, (s) -> s.remove(this.tip));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (this.breakingSession.contains(event.getPlayer().getName())) {
            return;
        }
        if (!event.getPlayer().isSneaking()) {
            return;
        }
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        String id = event.getBlock().getType().getKey().getKey();
        if (!isVeinMinedBlocks(id)) {
            return;
        }
        event.setCancelled(true);
        this.breakingSession.add(event.getPlayer().getName());
        mineBlockAt(event.getPlayer(), event.getBlock().getType(), event.getBlock(),0);
        this.breakingSession.remove(event.getPlayer().getName());
    }

    private boolean isVeinMinedBlocks(String id){
        return id.contains("_ore")
                ||id.contains("_log")
                ||id.contains("_leaves");
    }


    private void mineBlockAt(Player player, Material origin, Block block, int currentDeep) {
        if(!isVeinMinedBlocks(block.getType().getKey().getKey())){
            return;
        }

        if (block.getType() != origin) {
            return;
        }

        if (currentDeep >= MAX_DEEP) {
            return;
        }

        player.breakBlock(block);
        for (Block b : getAdjacentBlocks(block)) {
            mineBlockAt(player, origin, b, currentDeep + 1);
        }
    }

    private boolean testBlock(Block mining, Block original) {
        Material material = mining.getType();
        String id = material.getKey().getKey();

        if (id.contains("_ore")) {
            return mining.getType() == original.getType();
        }

        if (id.contains("_log")) {

            Axis origin = ((Orientable) original.getBlockData()).getAxis();
            Axis current = ((Orientable) mining.getBlockData()).getAxis();

            return mining.getType() == original.getType() && origin == current;
        }

        return false;
    }


    private List<Block> getAdjacentBlocks(Block block) {
        List<Block> adjacentBlocks = new ArrayList<>();
        World world = block.getWorld();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    if (xOffset == 0 && yOffset == 0 && zOffset == 0) {
                        continue; // Skip the center block
                    }
                    Block relativeBlock = world.getBlockAt(x + xOffset, y + yOffset, z + zOffset);
                    adjacentBlocks.add(relativeBlock);
                }
            }
        }

        return adjacentBlocks;
    }
}
