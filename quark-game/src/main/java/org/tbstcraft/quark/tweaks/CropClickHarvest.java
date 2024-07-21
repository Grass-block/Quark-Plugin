package org.tbstcraft.quark.tweaks;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

@QuarkModule(version = "1.0.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class CropClickHarvest extends PackageModule {
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
