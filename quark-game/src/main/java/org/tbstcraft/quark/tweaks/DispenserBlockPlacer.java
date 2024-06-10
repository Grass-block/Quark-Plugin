package org.tbstcraft.quark.tweaks;

import io.papermc.paper.event.block.BlockPreDispenseEvent;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

@QuarkModule(version = "1.0.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class DispenserBlockPlacer extends PackageModule {
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
