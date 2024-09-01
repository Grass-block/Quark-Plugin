package org.tbstcraft.quark.tweaks;

import me.gb2022.commons.reflect.AutoRegister;
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
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.*;
import java.util.regex.Pattern;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(id = "vein_miner")
public final class VeinMiner extends PackageModule {
    private final Set<String> breakingSession = new HashSet<>();

    @Inject("tip")
    private LanguageItem tip;

    private Pattern pattern;

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
        this.pattern = this.getConfig().getRegex("regex");
    }

    @Override
    public void disable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
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
        if (canNotChainMine(id)) {
            return;
        }
        event.setCancelled(true);
        this.breakingSession.add(event.getPlayer().getName());
        mineBlockAt(event.getPlayer(), event.getBlock().getType(), event.getBlock(), 0);
        this.breakingSession.remove(event.getPlayer().getName());
    }

    private boolean canNotChainMine(String id) {
        return !this.pattern.matcher(id).find()||id.contains("stripped");
    }


    private void mineBlockAt(Player player, Material origin, Block block, int currentDeep) {
        if (canNotChainMine(block.getType().getKey().getKey())) {
            return;
        }

        if (block.getType() != origin) {
            return;
        }

        if (currentDeep >= getConfig().getInt("max-iterations")) {
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
