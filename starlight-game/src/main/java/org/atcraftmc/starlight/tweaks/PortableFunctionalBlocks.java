package org.atcraftmc.starlight.tweaks;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import me.gb2022.modular.subcomponent.ComponentProvider;
import me.gb2022.modular.subcomponent.SubComponent;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.SLModuleComponent;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

@ApplicationModule(id="portable-functional-blocks")
@AutoRegister(Registrations.SERVER_EVENT)
@ComponentProvider({PortableFunctionalBlocks.NetherUpdate.class, PortableFunctionalBlocks.VillageUpdate.class})
public final class PortableFunctionalBlocks extends SLPackageModule {

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
                for (SubComponent<?> c : this.handle().getComponents().values()) {
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

    public static final class NetherUpdate extends SLModuleComponent<PortableFunctionalBlocks> implements InventoryActionHandler {
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

    public static final class VillageUpdate extends SLModuleComponent<PortableFunctionalBlocks> implements InventoryActionHandler {
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
