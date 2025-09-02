package org.atcraftmc.quark.security;

import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
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
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.data.ModuleDataService;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.core.objects.SimpleRegion;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.component.Components;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.data.record.RecordEntry;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(ExplosionDefender.ExplosionWhitelistCommand.class)
@SLModule(version = "1.3.3")
@Components(ExplosionDefender.BlockExplosionListener.class)
public final class ExplosionDefender extends PackageModule {
    private final HashMap<String, SimpleRegion> whiteListedRegions = new HashMap<>();

    @Inject("explosion-defender;Time,World,X,Y,Z,Type")
    private RecordEntry record;

    @Override
    public void enable() {
        this.loadRegions();
        this.record.open();
    }

    @Override
    public void disable() {
        this.saveRegions();
        this.whiteListedRegions.clear();
        this.record.close();
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
        PluginMessenger.broadcastMapped("quark:explosion", (b) -> b.put("loc", loc));
        if (ConfigAccessor.getBool(this.getConfig(), "override-explosion")) {
            Objects.requireNonNull(loc.getWorld()).createExplosion(loc, 4f, false, false);
        }
        if (ConfigAccessor.getBool(this.getConfig(), "broadcast")) {
            MessageAccessor.broadcast(
                    this.getLanguage(),
                    true,
                    false,
                    "exploded",
                    Objects.requireNonNull(loc.getWorld()).getName(),
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ(),
                    explodedId
            );
        }
        if (ConfigAccessor.getBool(this.getConfig(), "record")) {
            this.record.addLine(
                    SharedObjects.DATE_FORMAT.format(new Date()),
                    Objects.requireNonNull(loc.getWorld()).getName(),
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ(),
                    explodedId
            );
        }
    }

    @QuarkCommand(name = "explosion-whitelist", permission = "-quark.explosion.whitelist")
    public static final class ExplosionWhitelistCommand extends ModuleCommand<ExplosionDefender> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            String operation = args[0];
            if (Objects.equals(operation, "list")) {
                MessageAccessor.send(this.getLanguage(), sender, "region-list");
                Map<String, SimpleRegion> map = this.getModule().getWhiteListedRegions();
                for (String s : map.keySet()) {
                    sender.sendMessage(PlaceHolderService.format("{#gold}%s {#gray}-> {#white}%s".formatted(
                            s,
                            map.get(s)
                                    .toString()
                    )));
                }
                return;
            }
            String arg2 = args[1];
            if (Objects.equals(operation, "add")) {
                this.checkException(args.length == 9);
                if (this.getModule().getWhiteListedRegions().containsKey(arg2)) {
                    MessageAccessor.send(this.getLanguage(), sender, "region-add-failed", arg2);
                    return;
                }
                this.getModule().getWhiteListedRegions().put(arg2, new SimpleRegion(
                        Bukkit.getWorld(args[2]),
                        Integer.parseInt(args[3]),
                        Integer.parseInt(args[4]),
                        Integer.parseInt(args[5]),
                        Integer.parseInt(args[6]),
                        Integer.parseInt(args[7]),
                        Integer.parseInt(args[8])
                ));
                MessageAccessor.send(this.getLanguage(), sender, "region-add", arg2);
                this.getModule().saveRegions();
                return;
            }
            if (Objects.equals(operation, "remove")) {
                this.checkException(args.length == 2);
                if (!this.getModule().getWhiteListedRegions().containsKey(arg2)) {
                    MessageAccessor.send(this.getLanguage(), sender, "region-remove-failed", arg2);
                    throw new RuntimeException("???");
                }
                this.getModule().getWhiteListedRegions().remove(arg2);
                MessageAccessor.send(this.getLanguage(), sender, "region-remove", arg2);
            }
        }

        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, "add", "remove", "list");
            suggestion.matchArgument(0, "add", (s) -> s.suggest(1, "[name]"));
            suggestion.matchArgument(0, "remove", (s) -> {
                var c = this.getModule().getWhiteListedRegions().keySet();
                s.suggest(1, c);
            });

            var sender = suggestion.getSender();
            var isPlayer = sender instanceof Player;

            suggestion.matchArgument(0, "add", (s) -> {
                s.suggest(2, Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toSet()));
                s.suggest(3, isPlayer ? String.valueOf(suggestion.getSenderAsPlayer().getLocation().getBlockX()) : "0");
                s.suggest(4, isPlayer ? String.valueOf(suggestion.getSenderAsPlayer().getLocation().getBlockY()) : "0");
                s.suggest(5, isPlayer ? String.valueOf(suggestion.getSenderAsPlayer().getLocation().getBlockZ()) : "0");
                s.suggest(6, isPlayer ? String.valueOf(suggestion.getSenderAsPlayer().getLocation().getBlockX()) : "3");
                s.suggest(7, isPlayer ? String.valueOf(suggestion.getSenderAsPlayer().getLocation().getBlockY()) : "3");
                s.suggest(8, isPlayer ? String.valueOf(suggestion.getSenderAsPlayer().getLocation().getBlockZ()) : "3");
            });
        }
    }

    @AutoRegister(ServiceType.EVENT_LISTEN)
    public static final class BlockExplosionListener extends ModuleComponent<ExplosionDefender> {

        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("org.bukkit.event.block.BlockExplodeEvent"));
        }

        @EventHandler
        public void onBlockExplode(BlockExplodeEvent event) {
            Block b = event.getBlock();

            if (this.parent.matchRegion(b.getLocation())) {
                return;
            }
            event.setCancelled(true);
            this.parent.handle(b.getLocation(), "[?]");
        }
    }
}