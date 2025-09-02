package org.atcraftmc.quark.security;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
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
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.data.ModuleDataService;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.core.objects.Region;
import org.atcraftmc.starlight.core.objects.SimpleRegion;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.data.record.RecordEntry;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider({LimitedArea.LimitedAreaCommand.class})
@SLModule(version = "1.3.0")
public final class LimitedArea extends PackageModule {
    private final HashMap<String, SimpleRegion> regions = new HashMap<>();

    @Inject("limited-area;Time,Player,World,X,Y,Z,Region")
    private RecordEntry record;

    @Override
    public void enable() {
        this.loadRegions();
    }

    @Override
    public void disable() {
        this.saveRegions();
        this.regions.clear();
    }

    public void loadRegions() {
        NBTTagCompound tag = ModuleDataService.getEntry(this.getId());
        this.regions.clear();
        for (String s : tag.getTagMap().keySet()) {
            this.regions.put(s, new SimpleRegion(tag.getCompoundTag(s)));
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
            Player p = event.getPlayer();
            if (Objects.requireNonNull(p.getPlayer()).isOp()) {
                return;
            }
            Region r = WESessionTrackService.getRegion(p);
            for (Region s : this.regions.values()) {
                if (s.asAABB().inbound(r.asAABB())) {
                    return;
                }
            }
            event.setCancelled(true);
            MessageAccessor.send(this.getLanguage(), p, "interact_blocked_we");

            if (!ConfigAccessor.getBool(this.getConfig(), "record")) {
                return;
            }
            this.record.addLine(
                    SharedObjects.DATE_FORMAT.format(new Date()),
                    p.getName(),
                    p.getLocation().getWorld().getName(),
                    p.getLocation().getBlockX(),
                    p.getLocation().getBlockY(),
                    p.getLocation().getBlockZ(),
                    r.toString()
                                    );
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
        MessageAccessor.send(this.getLanguage(), p, "interact_blocked");
        if (!ConfigAccessor.getBool(this.getConfig(), "record")) {
            return;
        }
        this.record.addLine(
                SharedObjects.DATE_FORMAT.format(new Date()),
                p.getName(),
                p.getLocation().getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ(),
                ""
                                );
    }

    public HashMap<String, SimpleRegion> getRegions() {
        return regions;
    }

    @QuarkCommand(name = "limit-area", op = true)
    public static final class LimitedAreaCommand extends ModuleCommand<LimitedArea> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            String arg2 = args[1];
            switch (args[0]) {
                case "add" -> {
                    this.checkException(args.length == 9);
                    if (this.getModule().getRegions().containsKey(arg2)) {
                        MessageAccessor.send(this.getLanguage(), sender, "region_add_failed", arg2);
                        return;
                    }
                    this.getModule().getRegions().put(arg2, new SimpleRegion(
                            Bukkit.getWorld(args[2]),
                            Integer.parseInt(args[3]),
                            Integer.parseInt(args[4]),
                            Integer.parseInt(args[5]),
                            Integer.parseInt(args[6]),
                            Integer.parseInt(args[7]),
                            Integer.parseInt(args[8])
                    ));
                    MessageAccessor.send(this.getLanguage(), sender, "region_add", arg2);
                    this.getModule().saveRegions();
                }
                case "remove" -> {
                    this.checkException(args.length == 2);
                    if (!this.getModule().getRegions().containsKey(arg2)) {
                        MessageAccessor.send(this.getLanguage(), sender, "region_remove_failed", arg2);
                        throw new RuntimeException("???");
                    }
                    this.getModule().getRegions().remove(arg2);
                    MessageAccessor.send(this.getLanguage(), sender, "region_remove", arg2);
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("add");
                tabList.add("remove");
                tabList.add("record");
                return;
            }

            switch (buffer[0]) {
                case "add" -> {
                    switch (buffer.length) {
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
                    if (buffer.length != 2) {
                        return;
                    }
                    tabList.addAll(this.getModule().getRegions().keySet());
                }
            }
        }
    }
}