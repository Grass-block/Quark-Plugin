package org.atcraftmc.quark.tweaks;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.foundation.platform.BukkitDataAccess;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SLModule(version = "1.0.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class CropClickHarvest extends PackageModule {
    public static final Map<Material, Material> CROPS = new HashMap<>();
    public static final double X_RANGE = 0.15;
    public static final double Y_RANGE = 0.35;

    static {
        CROPS.put(Material.WHEAT, Material.WHEAT_SEEDS);
        CROPS.put(Material.BEETROOTS, Material.BEETROOT);
        CROPS.put(Material.POTATOES, Material.POTATO);
        CROPS.put(Material.CARROTS, Material.CARROT);
    }

    static void processSeedReuses(Collection<ItemStack> items, Material seed) {
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

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }

        var block = event.getClickedBlock();
        var type = block.getType();
        var random = SharedObjects.RANDOM;

        if (!CROPS.containsKey(type)) {
            return;
        }

        var data = BukkitDataAccess.blockData(block, Ageable.class);

        Collection<ItemStack> items = event.getItem() != null ? block.getDrops(event.getItem(), event.getPlayer()) : block.getDrops();

        if (data.getAge() != data.getMaximumAge()) {
            return;
        }

        data.setAge(0);
        block.setBlockData(data);

        processSeedReuses(items, CROPS.get(type));

        for (ItemStack stack : items) {
            Location loc = block.getLocation();
            loc.add(random.nextDouble(-X_RANGE, X_RANGE), random.nextDouble(X_RANGE, Y_RANGE), random.nextDouble(-X_RANGE, X_RANGE));
            ((Item) block.getWorld().spawnEntity(loc, EntityType.DROPPED_ITEM)).setItemStack(stack);
        }
    }
}
