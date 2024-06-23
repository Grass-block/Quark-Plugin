package org.tbstcraft.quark.security;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.data.config.Queries;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.compat.Compat;
import org.tbstcraft.quark.framework.module.compat.CompatContainer;
import org.tbstcraft.quark.framework.module.compat.CompatDelegate;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.data.ModuleDataService;
import org.tbstcraft.quark.foundation.platform.APIProfile;
import org.tbstcraft.quark.foundation.region.SimpleRegion;

import java.util.*;

@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(ExplosionDefender.ExplosionWhitelistCommand.class)
@QuarkModule(version = "1.3.3", recordFormat = {"Time", "World", "X", "Y", "Z", "Type"})
@Compat(ExplosionDefender.LegacyCompat.class)
public final class ExplosionDefender extends PackageModule {
    private final HashMap<String, SimpleRegion> whiteListedRegions = new HashMap<>();

    @Override
    public void enable() {
        this.loadRegions();
    }

    @Override
    public void disable() {
        this.saveRegions();
        this.whiteListedRegions.clear();
    }

    public void loadRegions() {
        NBTTagCompound tag = ModuleDataService.getEntry(this.getId());
        this.whiteListedRegions.clear();
        for (String s : tag.getTagMap().keySet()) {
            this.whiteListedRegions.put(s, new SimpleRegion(tag.getCompoundTag(s)));
        }
    }

    public void saveRegions() {
        NBTTagCompound tag = ModuleDataService.getEntry(this.getId());
        for (String s : this.whiteListedRegions.keySet()) {
            tag.setCompoundTag(s, this.whiteListedRegions.get(s).serialize());
        }
        ModuleDataService.save(this.getId());
    }

    public HashMap<String, SimpleRegion> getWhiteListedRegions() {
        return whiteListedRegions;
    }

    public boolean matchRegion(Location loc) {
        for (SimpleRegion s : this.whiteListedRegions.values()) {
            if (s.inBound(loc)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity e = event.getEntity();
        if (matchRegion(e.getLocation())) {
            return;
        }
        event.setCancelled(true);
        this.handle(e.getLocation(), e.getType().getKey().toString());
    }

    public void handle(Location loc, String explodedId) {
        if (this.getConfig().getBoolean("override-explosion")) {
            Objects.requireNonNull(loc.getWorld()).createExplosion(loc, 4f, false, false);
        }
        if (this.getConfig().getBoolean("broadcast")) {
            this.getLanguage().broadcastMessage(true, false, "exploded",
                    Objects.requireNonNull(loc.getWorld()).getName(),
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ(),
                    explodedId
            );
        }
        if (this.getConfig().getBoolean("record")) {
            this.getRecord().addLine(
                    SharedObjects.DATE_FORMAT.format(new Date()),
                    Objects.requireNonNull(loc.getWorld()).getName(),
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ(),
                    explodedId);
        }
    }


    @QuarkCommand(name = "explosion-whitelist", op = true)
    public static final class ExplosionWhitelistCommand extends ModuleCommand<ExplosionDefender> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            String operation = args[0];
            if (Objects.equals(operation, "list")) {
                this.getLanguage().sendMessage(sender, "region-list");
                Map<String, SimpleRegion> map = this.getModule().getWhiteListedRegions();
                for (String s : map.keySet()) {
                    sender.sendMessage(Queries.GLOBAL_TEMPLATE_ENGINE.handle("{#gold}%s {#gray}-> {#white}%s".formatted(s, map.get(s).toString())));
                }
                return;
            }
            String arg2 = args[1];
            if (Objects.equals(operation, "add")) {
                this.checkException(args.length == 9);
                if (this.getModule().getWhiteListedRegions().containsKey(arg2)) {
                    this.getLanguage().sendMessage(sender, "region-add-failed", arg2);
                    return;
                }
                this.getModule().getWhiteListedRegions().put(arg2, new SimpleRegion(Bukkit.getWorld(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8])));
                this.getLanguage().sendMessage(sender, "region-add", arg2);
                this.getModule().saveRegions();
                return;
            }
            if (Objects.equals(operation, "remove")) {
                this.checkException(args.length == 2);
                if (!this.getModule().getWhiteListedRegions().containsKey(arg2)) {
                    this.getLanguage().sendMessage(sender, "region-remove-failed", arg2);
                    throw new RuntimeException("???");
                }
                this.getModule().getWhiteListedRegions().remove(arg2);
                this.getLanguage().sendMessage(sender, "region-remove", arg2);
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("add");
                tabList.add("remove");
                tabList.add("list");
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
                        case 4, 7 ->
                                tabList.add(sender instanceof Player ? String.valueOf(((Player) sender).getLocation().getBlockX()) : "0");
                        case 5, 8 ->
                                tabList.add(sender instanceof Player ? String.valueOf(((Player) sender).getLocation().getBlockY()) : "0");
                        case 6, 9 ->
                                tabList.add(sender instanceof Player ? String.valueOf(((Player) sender).getLocation().getBlockZ()) : "0");
                    }
                }
                case "remove" -> {
                    if (buffer.length != 2) {
                        return;
                    }
                    tabList.addAll(this.getModule().getWhiteListedRegions().keySet());
                }
                case "record" -> {
                    if (buffer.length + 1 > 2) {
                        return;
                    }
                    tabList.add("true");
                    tabList.add("false");
                }
            }
        }
    }

    @CompatDelegate(APIProfile.BUKKIT)
    public static final class LegacyCompat extends CompatContainer<ExplosionDefender> {
        public LegacyCompat(ExplosionDefender parent) {
            super(parent);
        }

        @EventHandler
        public void onBlockExplode(BlockExplodeEvent event) {
            Block b = event.getBlock();

            if (getParent().matchRegion(b.getLocation())) {
                return;
            }
            event.setCancelled(true);
            this.getParent().handle(b.getLocation(), "[?]");
        }
    }
}