package org.tbstcraft.quark.tweaks;

import io.papermc.paper.event.block.BlockPreDispenseEvent;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

@QuarkModule(version = "1.0.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class DispenserBlockPlacer extends PackageModule {

    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void checkCompatibility() {
        Compatibility.requireClass(() -> Class.forName("io.papermc.paper.event.block.BlockPreDispenseEvent"));
        Compatibility.requirePDC();
    }

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
    }

    @Override
    public void disable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
    }

    @EventHandler
    public void dispenserPlaceBlock(BlockPreDispenseEvent event) {
        Material material = event.getItemStack().getType();

        Directional data = (Directional) event.getBlock().getBlockData();

        event.getBlock().getRelative(data.getFacing());
        Block facing = event.getBlock().getRelative(data.getFacing());

        if (material.isBlock() && facing.getType().isAir()) {
            if (material == Material.TNT) {
                return;
            }

            event.setCancelled(true);

            facing.setBlockData(event.getItemStack().getType().createBlockData());

            ItemStack dispensed = ((Dispenser) event.getBlock().getState()).getInventory().getItem(event.getSlot());
            if (dispensed != null) {
                dispensed.setAmount(dispensed.getAmount() - 1);
            }
            return;
        }

        if (facing.getType().isBlock() && facing.isValidTool(event.getItemStack())) {
            event.setCancelled(true);

            facing.breakNaturally(event.getItemStack(), true);
        }
    }
}
