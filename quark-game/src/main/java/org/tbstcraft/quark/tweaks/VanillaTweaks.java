package org.tbstcraft.quark.tweaks;

import io.papermc.paper.event.block.BlockPreDispenseEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.util.api.BukkitUtil;

import java.util.*;

@QuarkModule(version = "1.2.0")
public class VanillaTweaks extends PackageModule {
    private static final Map<String, Listener> FEATURES = new HashMap<>();

    private static void initializeFeatures() {
        FEATURES.put("double-door-sync",new DoubleDoorSync());
        FEATURES.put("dispenser-block-placing",new DoubleDoorSync());
        FEATURES.put("crop-click-harvest",new CropClickHarvest());
    }

    @Override
    public void enable() {
        initializeFeatures();
        for (String k:FEATURES.keySet()) {
            if(!this.getConfig().getBoolean(k)){
                continue;
            }
            BukkitUtil.registerEventListener(FEATURES.get(k));
        }
    }

    @Override
    public void disable() {
        for (String k:FEATURES.keySet()) {
            BukkitUtil.unregisterEventListener(FEATURES.get(k));
        }
    }

    public static final class DoubleDoorSync implements Listener {

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock == null || isNotWoodenDoor(clickedBlock.getType())) {
                return;
            }
            checkDoors(clickedBlock);
        }

        private void checkDoors(Block dest) {
            Door data = (Door) dest.getBlockData();

            BlockFace destFace = switch (data.getFacing()) {
                case WEST -> BlockFace.SOUTH;
                case EAST -> BlockFace.NORTH;
                case NORTH -> BlockFace.WEST;
                case SOUTH -> BlockFace.EAST;
                default -> throw new IllegalStateException("Unexpected value: " + data.getFacing());
            };
            if (data.getHinge() == Door.Hinge.LEFT) {
                destFace = destFace.getOppositeFace();
            }

            Block pair = dest.getRelative(destFace);
            if (isNotWoodenDoor(pair.getType())) {
                return;
            }
            Door pairData = ((Door) pair.getBlockData());

            if (pairData.getHinge() == data.getHinge()) {
                return;
            }
            if (pairData.getHalf() != data.getHalf()) {
                return;
            }

            pairData.setOpen(!data.isOpen());
            pair.setBlockData(pairData);

            Block pairHalf = pair.getRelative(pairData.getHalf() == Bisected.Half.BOTTOM ? BlockFace.UP : BlockFace.DOWN);
            Door pairHalfData = (Door) pairHalf.getBlockData();
            pairHalfData.setOpen(!data.isOpen());
            pairHalf.setBlockData(pairHalfData);
        }

        private boolean isNotWoodenDoor(Material material) {
            return material != Material.OAK_DOOR && material != Material.ACACIA_DOOR &&
                    material != Material.BIRCH_DOOR && material != Material.DARK_OAK_DOOR &&
                    material != Material.JUNGLE_DOOR && material != Material.SPRUCE_DOOR;
        }

    }

    public static final class DispenserBlockPlacer implements Listener {
        @EventHandler
        public void dispenserPlaceBlock(BlockPreDispenseEvent event) {
            if (!event.getItemStack().getType().isBlock()) {
                return;
            }
            if (event.getItemStack().getType() == Material.TNT) {
                return;
            }
            Directional data = (Directional) event.getBlock().getBlockData();
            Block b = event.getBlock().getRelative(data.getFacing());
            if (!b.getType().isAir()) {
                return;
            }
            b.setBlockData(event.getItemStack().getType().createBlockData());
            event.setCancelled(true);
            ItemStack dispensed = ((Dispenser) event.getBlock().getState()).getInventory().getItem(event.getSlot());
            if (dispensed != null) {
                dispensed.setAmount(dispensed.getAmount() - 1);
            }
        }
    }

    public static final class CropClickHarvest implements Listener {
        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            Block block = event.getClickedBlock();
            if (block == null) {
                return;
            }
            Material type = block.getType();
            if (isNotCrops(block.getType())) {
                return;
            }

            Collection<ItemStack> items = event.getItem() != null ? block.getDrops(event.getItem(), event.getPlayer()) : block.getDrops();

            if (type == Material.WHEAT) {
                this.handleSeeds(items, Material.WHEAT_SEEDS);
            }
            if (type == Material.BEETROOTS) {
                this.handleSeeds(items, Material.BEETROOT_SEEDS);
            }
            if (type == Material.CARROTS) {
                this.handleSeeds(items, Material.CARROT);
            }
            if (type == Material.POTATOES) {
                this.handleSeeds(items, Material.POTATO);
            }

            Random r = new Random();
            for (ItemStack stack : items) {
                Location loc = block.getLocation();
                loc.add(r.nextDouble(-0.15, 0.15), r.nextDouble(0.15, 0.35), r.nextDouble(-0.15, 0.15));
                block.getWorld().spawn(loc, Item.class, (e) -> e.setItemStack(stack));
            }


            Ageable data = ((Ageable) block.getBlockData());
            if (data.getAge() != data.getMaximumAge()) {
                return;
            }
            data.setAge(0);
            block.setBlockData(data);
        }

        void handleSeeds(Collection<ItemStack> items, Material seed) {
            Iterator<ItemStack> it = items.iterator();

            while (it.hasNext()) {
                ItemStack stack = it.next();
                if (stack.getType() != seed) {
                    continue;
                }
                stack.setAmount(stack.getAmount() - 1);
                if (stack.getAmount() <= 0) {
                    it.remove();
                    return;
                }
            }
        }

        boolean isNotCrops(Material material) {
            return material != Material.WHEAT
                    && material != Material.POTATOES
                    && material != Material.CARROTS
                    && material != Material.BEETROOTS;
        }
    }
}
