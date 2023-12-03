package org.tbstcraft.quark.security;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.service.ModuleDataService;
import org.tbstcraft.quark.service.WorldEditLocalSessionTracker;
import org.tbstcraft.quark.util.Region;
import org.tbstcraft.quark.util.nbt.NBTTagCompound;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@QuarkModule
public final class LimitedArea extends PluginModule {
    private final HashMap<String, Region> regions = new HashMap<>();

    private final CommandHandler command = new CommandHandler(this);

    @Override
    public void onEnable() {
        this.registerCommand(this.command);
        this.registerListener();
        this.loadRegions();
    }

    @Override
    public void onDisable() {
        this.unregisterCommand(this.command);
        this.unregisterListener();
        this.saveRegions();
        this.regions.clear();
    }

    public void loadRegions() {
        NBTTagCompound tag = ModuleDataService.getEntry(this.getId());
        this.regions.clear();
        for (String s : tag.getTagMap().keySet()) {
            this.regions.put(s, new Region(tag.getCompoundTag(s)));
        }
    }

    public void saveRegions() {
        NBTTagCompound tag = ModuleDataService.getEntry(this.getId());
        for (String s : this.regions.keySet()) {
            tag.setCompoundTag(s, this.regions.get(s).serialize());
        }
        ModuleDataService.save(this.getId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        ItemStack i = event.getPlayer().getInventory().getItem(event.getPlayer().getInventory().getHeldItemSlot());
        if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")
                && i != null
                && i.getType() == Material.WOODEN_AXE
                && event.getPlayer().hasPermission("worldedit.region.*")
        ) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        this.check(event.getClickedBlock().getLocation(), event.getPlayer(), event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        this.check(event.getBlock().getLocation(), event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldEditCommand(PlayerCommandPreprocessEvent event) {
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            return;
        }
        if (event.getMessage().startsWith("//pos1")) {
            return;
        }
        if (event.getMessage().startsWith("//pos2")) {
            return;
        }
        if (event.getMessage().startsWith("//")) {
            Player player = event.getPlayer();
            if (Objects.requireNonNull(player.getPlayer()).isOp()) {
                return;
            }
            Region r = WorldEditLocalSessionTracker.getRegion(player);
            for (Region s : this.regions.values()) {
                if (s.asAABB().inbound(r.asAABB())) {
                    return;
                }
            }
            event.setCancelled(true);
            this.getLanguage().sendMessageTo(player, "interact_blocked_we");

            if (!this.getConfig().getBoolean("record")) {
                return;
            }
            this.getRecordEntry().record("[%s]player:%s world:%s session:%s".formatted(new SimpleDateFormat().format(new Date()), player.getName(), Objects.requireNonNull(event.getPlayer().getEyeLocation().getWorld()).getName(), r.toString()));
        }
    }

    public void check(Location loc, Player p, Cancellable event) {
        if (p.isOp()) {
            return;
        }
        for (Region s : this.regions.values()) {
            if (s.inBound(loc)) {
                return;
            }
        }
        event.setCancelled(true);
        this.getLanguage().sendMessageTo(p, "interact_blocked");
        if (!this.getConfig().getBoolean("record")) {
            return;
        }
        this.getRecordEntry().record("[%s]player:%s world:%s pos:%s,%s,%s".formatted(
                new SimpleDateFormat().format(new Date()),
                p.getName(),
                Objects.requireNonNull(loc.getWorld()).getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        ));
    }

    public HashMap<String, Region> getRegions() {
        return regions;
    }

    @QuarkCommand(name = "limitarea", op = true)
    public static final class CommandHandler extends ModuleCommand<LimitedArea> {
        public CommandHandler(LimitedArea module) {
            super(module);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            String arg2 = args[1];
            switch (args[0]) {
                case "add" -> {
                    this.checkException(args.length == 9);
                    if (this.getModule().getRegions().containsKey(arg2)) {
                        this.getLanguage().sendMessageTo(sender, "region_add_failed", arg2);
                        return;
                    }
                    this.getModule().getRegions().put(arg2, new Region(
                            Bukkit.getWorld(args[2]),
                            Integer.parseInt(args[3]),
                            Integer.parseInt(args[4]),
                            Integer.parseInt(args[5]),
                            Integer.parseInt(args[6]),
                            Integer.parseInt(args[7]),
                            Integer.parseInt(args[8])
                    ));
                    this.getLanguage().sendMessageTo(sender, "region_add", arg2);
                    this.getModule().saveRegions();
                }
                case "remove" -> {
                    this.checkException(args.length == 2);
                    if (!this.getModule().getRegions().containsKey(arg2)) {
                        this.getLanguage().sendMessageTo(sender, "region_remove_failed", arg2);
                        throw new RuntimeException("???");
                    }
                    this.getModule().getRegions().remove(arg2);
                    this.getLanguage().sendMessageTo(sender, "region_remove", arg2);
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
            if (args.length == 1) {
                tabList.add("add");
                tabList.add("remove");
                tabList.add("record");
                return;
            }

            switch (args[0]) {
                case "add" -> {
                    switch (args.length) {
                        case 3 -> {
                            for (World world : Bukkit.getWorlds()) {
                                tabList.add(world.getName().toLowerCase());
                            }
                        }
                        case 4, 7 -> tabList.add(sender instanceof Player ?
                                String.valueOf(((Player) sender).getLocation().getBlockX()) : "0");
                        case 5, 8 -> tabList.add(sender instanceof Player ?
                                String.valueOf(((Player) sender).getLocation().getBlockY()) : "0");
                        case 6, 9 -> tabList.add(sender instanceof Player ?
                                String.valueOf(((Player) sender).getLocation().getBlockZ()) : "0");
                    }
                }
                case "remove" -> {
                    if (args.length != 2) {
                        return;
                    }
                    tabList.addAll(this.getModule().getRegions().keySet());
                }
            }
        }
    }
}