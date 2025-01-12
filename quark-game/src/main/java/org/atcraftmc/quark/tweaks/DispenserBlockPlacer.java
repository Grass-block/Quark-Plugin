package org.atcraftmc.quark.tweaks;

import io.papermc.paper.event.block.BlockPreDispenseEvent;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.atcraftmc.qlib.language.LanguageItem;
import org.tbstcraft.quark.foundation.platform.BukkitDataAccess;
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
        var block = event.getBlock();
        var type = event.getItemStack().getType();
        var data = BukkitDataAccess.blockData(block, Directional.class);
        var face = block.getRelative(data.getFacing());

        if (type.isBlock() && face.getType().isAir()) {
            if (type == Material.TNT) {
                return;
            }

            var dispensed = BukkitDataAccess.blockState(block, Dispenser.class).getInventory().getItem(event.getSlot());

            if (dispensed != null) {
                dispensed.setAmount(dispensed.getAmount() - 1);
            }

            face.setBlockData(event.getItemStack().getType().createBlockData());
            event.setCancelled(true);

            return;
        }

        if (face.getType().isBlock() && face.isValidTool(event.getItemStack())) {
            event.setCancelled(true);
            face.breakNaturally(event.getItemStack(), true);
        }
    }
}
