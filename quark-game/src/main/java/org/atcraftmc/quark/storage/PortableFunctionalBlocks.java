package org.atcraftmc.quark.storage;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.component.Components;
import org.tbstcraft.quark.framework.module.component.ModuleComponent;
import org.tbstcraft.quark.framework.module.services.ServiceType;

@QuarkModule
@AutoRegister(ServiceType.EVENT_LISTEN)
@Components({PortableFunctionalBlocks.NetherUpdate.class, PortableFunctionalBlocks.VillageUpdate.class})
public final class PortableFunctionalBlocks extends PackageModule {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (event.getItem() == null) {
            return;
        }
        switch (event.getItem().getType()) {
            case CRAFTING_TABLE -> event.getPlayer().openWorkbench(null, true);
            //case ANVIL -> event.getPlayer().openInventory(Bukkit.createInventory(null, InventoryType.ANVIL)); bugggy
            case ENDER_CHEST -> event.getPlayer().openInventory(event.getPlayer().getEnderChest());
            default -> {
                for (ModuleComponent<?> c : this.getComponents().values()) {
                    if (!((InventoryActionHandler) c).handle(event.getPlayer(), event.getItem().getType())) {
                        continue;
                    }

                    return;
                }
            }
        }
    }

    interface InventoryActionHandler {
        boolean handle(Player player, Material holding);
    }

    public static final class NetherUpdate extends ModuleComponent<PortableFunctionalBlocks> implements InventoryActionHandler {
        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("org.bukkit.inventory.SmithingInventory"));
        }

        @Override
        public boolean handle(Player player, Material holding) {
            if (holding != Material.SMITHING_TABLE) {
                return false;
            }
            player.openSmithingTable(null, true);
            return true;
        }
    }

    public static final class VillageUpdate extends ModuleComponent<PortableFunctionalBlocks> implements InventoryActionHandler {
        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("org.bukkit.inventory.LoomInventory"));
            Compatibility.requireClass(() -> Class.forName("org.bukkit.inventory.StonecutterInventory"));
            Compatibility.requireClass(() -> Class.forName("org.bukkit.inventory.CartographyInventory"));
            Compatibility.requireClass(() -> Class.forName("org.bukkit.inventory.GrindstoneInventory"));
        }

        @Override
        public boolean handle(Player player, Material holding) {
            return switch (holding) {
                case LOOM -> {
                    player.openLoom(null, true);
                    yield true;
                }
                case STONECUTTER -> {
                    player.openStonecutter(null, true);
                    yield true;
                }
                case CARTOGRAPHY_TABLE -> {
                    player.openCartographyTable(null, true);
                    yield true;
                }
                case GRINDSTONE -> {
                    player.openGrindstone(null, true);
                    yield true;
                }
                default -> false;
            };
        }

    }
}
