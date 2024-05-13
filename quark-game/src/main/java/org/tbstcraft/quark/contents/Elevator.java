package org.tbstcraft.quark.contents;

import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.tbstcraft.quark.command.CommandRegistry;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.framework.customcontent.item.CustomItem;
import org.tbstcraft.quark.framework.customcontent.item.QuarkItem;
import org.tbstcraft.quark.framework.language.LanguageKey;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.util.api.BukkitUtil;
import org.tbstcraft.quark.util.api.PlayerUtil;
import org.tbstcraft.quark.util.crafting.RecipeBuilder;
import org.tbstcraft.quark.util.crafting.RecipeManager;

import java.util.Locale;
import java.util.Objects;

@EventListener
@CommandRegistry({Elevator.ElevatorItemCommand.class})
@QuarkModule(version = "1.0.0")
@SuppressWarnings("deprecation")
public final class Elevator extends PackageModule {
    public static final Recipe RECIPE = RecipeBuilder.shaped("elevator", "@#@;#*#;@#@",
            createElevatorItem(1),
            RecipeBuilder.symbol('#', Material.IRON_INGOT),
            RecipeBuilder.symbol('*', Material.PISTON),
            RecipeBuilder.symbol('@', Material.REDSTONE)
    );

    public static ItemStack createElevatorItem(int amount) {
        ItemStack stack = new ItemStack(Material.FURNACE, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return null;
        }
        meta.addEnchant(Enchantment.DURABILITY, 6, true);
        meta.setDisplayName("电梯");
        stack.setItemMeta(meta);
        BukkitUtil.setItemUsage(stack, "elevator");
        return stack;
    }

    @Override
    public void enable() {
        RecipeManager.register(RECIPE);
    }

    @Override
    public void disable() {
        RecipeManager.unregister(RECIPE);
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
        if (!isElevatorBlock(b)) {
            return;
        }

        int x = from.getBlockX();
        int y = from.getBlockY() + 1;
        int z = from.getBlockZ();

        double yo = player.getLocation().getY();

        while (y < world.getMaxHeight()) {
            if (isElevatorBlock(world.getBlockAt(x, y, z))) {
                PlayerUtil.teleport(player, player.getLocation().add(0, y + 1 - yo, 0));
                player.playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1, 0);
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
        if (!isElevatorBlock(b)) {
            return;
        }

        int x = from.getBlockX();
        int y = from.getBlockY() - 2;
        int z = from.getBlockZ();

        double yo = player.getLocation().getY();

        while (y > -65) {
            if (isElevatorBlock(world.getBlockAt(x, y, z))) {
                PlayerUtil.teleport(player, player.getLocation().add(0, y + 1 - yo, 0));
                player.playSound(player.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 1, 0);
                return;
            }
            y--;
        }
    }

    public boolean isElevatorBlock(Block b) {
        if (b.getType() != Material.FURNACE) {
            return false;
        }

        Furnace furnace = (Furnace) b.getState();

        if (!BukkitUtil.getBlockUsage(furnace).equals("elevator")) {
            return false;
        }

        Block b1 = b.getWorld().getBlockAt(b.getLocation().add(0, 1, 0));
        Block b2 = b.getWorld().getBlockAt(b.getLocation().add(0, 2, 0));
        return b1.getType().isAir() && b2.getType().isAir();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getItemInHand().getType() != Material.FURNACE) {
            return;
        }
        if (!BukkitUtil.checkUsage(event.getItemInHand(), "elevator")) {
            return;
        }

        Furnace furnace = (Furnace) event.getBlock().getState();
        BukkitUtil.setBlockUsage(furnace);
        furnace.update();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!isElevatorBlock(event.getBlock())) {
            return;
        }
        event.setDropItems(false);
        BukkitUtil.createDrop(block.getLocation(), createElevatorItem(1));
    }

    @QuarkCommand(name = "elevator", op = true, playerOnly = true)
    public static final class ElevatorItemCommand extends ModuleCommand<Elevator> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            ((Player) sender).getInventory().addItem(Objects.requireNonNull(createElevatorItem(64)));
        }
    }

    @QuarkItem(id = "quark:elevator", icon = Material.FURNACE, enchantGlow = true)
    public static final class ElevatorItem extends CustomItem {

        @Override
        public ComponentLike renderDisplayName(ItemStack stack, Locale locale) {
            return getLanguageKey().getMessageComponent(locale);
        }

        @Override
        public LanguageKey getLanguageKey() {
            return new LanguageKey(null, "elevator", "item-name");
        }
    }
}
