package org.atcraftmc.starlight.oddities;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.starlight.api.customization.CustomBlock;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.custom.CustomBlockService;
import org.atcraftmc.starlight.foundation.crafting.RecipeBuilder;
import org.atcraftmc.starlight.foundation.crafting.RecipeManager;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.foundation.platform.Players;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

@AutoRegister(Registrations.SERVER_EVENT)
@ApplicationModule(id = "elevator", version = "1.0.0")
@SuppressWarnings("deprecation")
public final class Elevator extends SLPackageModule {
    private final ElevatorBlock block = new ElevatorBlock();
    public Recipe recipe;
    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
        this.recipe = RecipeBuilder.shaped("elevator", "@#@;#*#;@#@",
                                           block.createItem(2),
                                           RecipeBuilder.symbol('#', Material.IRON_INGOT),
                                           RecipeBuilder.symbol('*', Material.PISTON),
                                           RecipeBuilder.symbol('@', Material.REDSTONE)
        );
        RecipeManager.register(recipe);
        CustomBlockService.instance().registerBlock(this.block);
    }

    @Override
    public void disable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
        RecipeManager.unregister(recipe);
        CustomBlockService.instance().unregisterBlock("elevator");
    }

    @EventHandler
    public void onPlayerJump(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();
        World world = from.getWorld();
        if (world == null) {
            return;
        }
        if (!BukkitUtil.testJump(to, from)) {
            return;
        }
        Block b = BukkitUtil.getSteppingBlock(from);
        if (b == null) {
            return;
        }
        if (!isValidElevator(b)) {
            return;
        }

        int x = from.getBlockX();
        int y = from.getBlockY() + 1;
        int z = from.getBlockZ();

        double yo = player.getLocation().getY();

        while (y < world.getMaxHeight()) {
            if (isValidElevator(world.getBlockAt(x, y, z))) {
                TaskService.future(Players.teleport(player, player.getLocation().add(0, y + 1 - yo, 0)), (v) -> {
                    var sound = Sound.BLOCK_PISTON_EXTEND;
                    player.playSound(player.getLocation(), sound, 1, 0);
                });
                return;
            }
            y++;
        }
    }

    @EventHandler
    public void onPlayerShift(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) {
            return;
        }
        Player player = event.getPlayer();
        Location from = player.getLocation();
        World world = player.getWorld();
        Block b = BukkitUtil.getSteppingBlock(from);
        if (b == null) {
            return;
        }
        if (!isValidElevator(b)) {
            return;
        }

        int x = from.getBlockX();
        int y = from.getBlockY() - 2;
        int z = from.getBlockZ();

        double yo = player.getLocation().getY();

        while (y > -65) {
            if (isValidElevator(world.getBlockAt(x, y, z))) {
                TaskService.future(Players.teleport(player, player.getLocation().add(0, y + 1 - yo, 0)), (v) -> {
                    var sound = Sound.BLOCK_PISTON_CONTRACT;
                    player.playSound(player.getLocation(), sound, 1, 0);
                });
                return;
            }
            y--;
        }
    }

    public boolean isValidElevator(Block b) {
        if (!this.block.test(b)) {
            return false;
        }

        var b1 = b.getWorld().getBlockAt(b.getLocation().add(0, 1, 0));
        var b2 = b.getWorld().getBlockAt(b.getLocation().add(0, 2, 0));
        return b1.getType().isAir() && b2.getType().isAir();
    }

    public final class ElevatorBlock extends CustomBlock {

        private ElevatorBlock() {
            super("elevator");
        }

        @Override
        public LanguageItem getDisplayName(ItemStack stack) {
            return Elevator.this.language().item("item-name");
        }

        @Override
        public LanguageItem getDescription(ItemStack stack) {
            return Elevator.this.language().item("item-lore");
        }

        @Override
        public Material getActualBlock() {
            return Material.FURNACE;
        }
    }
}
