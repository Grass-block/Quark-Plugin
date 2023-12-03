package org.tbstcraft.quark.internal.defense;

import org.tbstcraft.quark.Region;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.ModuleCommand;
import org.tbstcraft.quark.util.BukkitUtil;
import org.tbstcraft.quark.util.nbt.NBTTagCompound;
import org.tbstcraft.quark.util.registry.TypeItem;
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

import java.text.SimpleDateFormat;
import java.util.*;
import org.tbstcraft.quark.module.PluginModule;

@TypeItem("quark_defense:protection_area")
public final class ProtectionArea extends PluginModule {
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
        this.loadDataFile();
        NBTTagCompound tag = this.getDataTag();
        this.regions.clear();
        for (String s : tag.getTagMap().keySet()) {
            this.regions.put(s, new Region(tag.getCompoundTag(s)));
        }
    }

    public void saveRegions() {
        NBTTagCompound tag = this.getDataTag();
        for (String s : this.regions.keySet()) {
            tag.setCompoundTag(s, this.regions.get(s).serialize());
        }
        this.saveDataFile();
    }

    @Override
    public void displayInfo(CommandSender sender) {
        sender.sendMessage(BukkitUtil.formatChatComponent("""
                 {white}Protection-Area 1.4
                 {gray}  ——都说了几遍了,保护区域不要乱搞
                 {gold}----------------------------------------------
                 {white}作者: {gray}GrassBlock2022
                 {white}版权: {gray}©FlybirdGames 2015-2023
                 {white}ID: {gray}%s
                """).formatted(this.getModuleID(), this.getClass().getName()));
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
            Region r = SharedObjects.WE_SESSION_TRACKER.getRegion(player);
            for (Region s : this.regions.values()) {
                if (s.asAABB().intersects(r.asAABB())) {
                    event.setCancelled(true);
                    this.getConfig().sendMessageTo(player, "interact_blocked_we");

                    if (!this.getConfig().isEnabled(this.getId(), "record")) {
                        return;
                    }
                    this.record("[%s]player:%s world:%s session:%s".formatted(new SimpleDateFormat().format(new Date()), player.getName(), Objects.requireNonNull(event.getPlayer().getEyeLocation().getWorld()).getName(), r.toString()));
                    return;
                }
            }
        }
    }

    private void check(Location loc, Player player, Cancellable event) {
        if (Objects.requireNonNull(player.getPlayer()).isOp()) {
            return;
        }
        for (Region s : this.regions.values()) {
            if (s.inBound(loc)) {
                if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
                    continue;
                }
                event.setCancelled(true);
                this.getConfig().sendMessageTo(player, "interact_blocked");

                if (!this.getConfig().isEnabled(this.getId(), "record")) {
                    return;
                }
                this.record("[%s]player:%s world:%s pos:%s,%s,%s".formatted(new SimpleDateFormat().format(new Date()), player.getName(), Objects.requireNonNull(loc.getWorld()).getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                return;
            }
        }
    }

    public HashMap<String, Region> getRegions() {
        return regions;
    }


    @QuarkCommand(name = "protection-area", op = true)
    public static final class CommandHandler extends ModuleCommand<ProtectionArea> {
        public CommandHandler(ProtectionArea module) {
            super(module);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            String arg2 = args[1];

            String operation = args[0];
            if (Objects.equals(operation, "add")) {
                this.checkException(args.length == 9);
                if (this.getModule().getRegions().containsKey(arg2)) {
                    this.getModule().sendMessageTo(sender, "command_region_add_failed", arg2);
                    return;
                }
                this.getModule().getRegions().put(arg2, new Region(Bukkit.getWorld(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8])));
                this.getModule().sendMessageTo(sender, "command_region_add", arg2);
                this.getModule().saveRegions();
                return;
            }
            if (Objects.equals(operation, "remove")) {
                this.checkException(args.length == 2);
                if (!this.getModule().getRegions().containsKey(arg2)) {
                    this.getModule().sendMessageTo(sender, "command_region_remove_failed", arg2);
                    throw new RuntimeException("???");
                }
                this.getModule().getRegions().remove(arg2);
                this.getModule().sendMessageTo(sender, "command_region_remove", arg2);
                this.getModule().reloadConfig();
                return;
            }
            if (Objects.equals(operation, "list")) {
                this.getModule().sendMessageTo(sender, "command_region_list");
                Map<String, Region> map = this.getModule().getRegions();
                for (String s : map.keySet()) {
                    sender.sendMessage(BukkitUtil.formatChatComponent("{gold}%s: {white}%s".formatted(s, map.get(s).toString())));
                }
                return;
            }
            if (Objects.equals(operation, "record")) {
                this.checkException(args.length == 2);
                this.checkException(isBooleanOption(arg2));
                this.getModule().getConfig().getConfigSection().set("record", arg2);
                this.getModule().sendMessageTo(sender, "command_record_set", arg2);
                this.getModule().reloadConfig();

                if (arg2.equals("true")) {
                    this.getModule().openRecordStream();
                }
                if (arg2.equals("false")) {
                    this.getModule().closeRecordStream();
                }
                this.getModule().reloadConfig();
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
                        case 4, 7 ->
                                tabList.add(sender instanceof Player ? String.valueOf(((Player) sender).getLocation().getBlockX()) : "0");
                        case 5, 8 ->
                                tabList.add(sender instanceof Player ? String.valueOf(((Player) sender).getLocation().getBlockY()) : "0");
                        case 6, 9 ->
                                tabList.add(sender instanceof Player ? String.valueOf(((Player) sender).getLocation().getBlockZ()) : "0");
                    }
                }
                case "remove" -> {
                    if (args.length != 2) {
                        return;
                    }
                    tabList.addAll(this.getModule().getRegions().keySet());
                }
                case "record" -> {
                    if (args.length + 1 > 2) {
                        return;
                    }
                    tabList.add("true");
                    tabList.add("false");
                }
            }
        }


        @Override
        public String getUsage() {
            return """
                    以下是各个子命令(蓝色填入整数, 黄色填入名称, 绿色填入tab指定的选项):
                    {white} - 添加区域: /protection-area {green}add {yellow}[name] {green}[world] {aqua}[x] [y] [z] [x] [y] [z]
                    {white} - 移除区域: /protection-area {green}remove {yellow}[name]
                    {white} - 查看区域: /protection-area {green}list
                    {white} - 记录开关: /protection-area {green}record [operation]
                     """;
        }

        @Override
        public String getDescription() {
            return "对保护区域进行指定的操作。";
        }
    }
}