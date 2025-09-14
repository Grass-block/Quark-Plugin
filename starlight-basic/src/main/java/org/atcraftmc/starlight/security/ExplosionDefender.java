package org.atcraftmc.starlight.security;

import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.starlight.core.SimpleRegion;
import org.atcraftmc.starlight.core.data.RegionService;
import org.atcraftmc.starlight.core.objects.Region;
import org.atcraftmc.starlight.data.ModuleDataService;
import org.atcraftmc.starlight.data.record.RecordEntry;
import org.atcraftmc.starlight.data.record.registry.RecordField;
import org.atcraftmc.starlight.data.record.registry.RecordRegistry;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.component.Components;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.Registers;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.joml.Vector3d;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@AutoRegister(Registers.BUKKIT_EVENT)
@CommandProvider(ExplosionDefender.ExplosionWhitelistCommand.class)
@SLModule(version = "1.3.3")
@Components(ExplosionDefender.BlockExplosionListener.class)
public final class ExplosionDefender extends PackageModule {
    private static final RecordRegistry.A5<String, Number, Number, Number, String> RECORD = new RecordRegistry.A5<>(
            "explosions",
            RecordField.WORLD,
            RecordField.X,
            RecordField.Y,
            RecordField.Z,
            RecordField.TYPE
    );

    private final HashMap<String, SimpleRegion> whiteListedRegions = new HashMap<>();

    @Inject("starlight:default/explosion_whitelist")
    private RegionService service;

    @Inject("explosion-defender;Time,World,X,Y,Z,Type")
    private RecordEntry record;

    @Override
    public void enable() {
        this.record.open();
    }

    @Override
    public void disable() {
        this.record.close();
    }

    public HashMap<String, SimpleRegion> getWhiteListedRegions() {
        return whiteListedRegions;
    }

    public boolean matchRegion(Location loc) {
        try {
            return this.service.isAnyHit(loc);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        var e = event.getEntity();
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
            RECORD.render(Objects.requireNonNull(loc.getWorld()).getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), explodedId);
        }
    }

    @QuarkCommand(name = "explosion-whitelist", permission = "-quark.explosion.whitelist")
    public static final class ExplosionWhitelistCommand extends ModuleCommand<ExplosionDefender> {


        @Override
        public void execute(CommandExecution context) {
            var service = getModule().service;
            var sender = context.getSender();

            //todo: list

            try {
                switch (context.requireEnum(0, "list", "add", "remove")) {
                    case "add" -> {
                        var id = context.requireArgumentAt(1);

                        if (service.existName(id)) {
                            MessageAccessor.send(this.getLanguage(), sender, "region-add-failed", id);
                            return;
                        }

                        var p0 = new Vector3d(
                                context.requireArgumentDouble(3),
                                context.requireArgumentDouble(4),
                                context.requireArgumentDouble(5)
                        );

                        var p1 = new Vector3d(
                                context.requireArgumentDouble(6),
                                context.requireArgumentDouble(7),
                                context.requireArgumentDouble(8)
                        );

                        service.add(new Region(context.requireArgumentAt(1), context.requireArgumentAt(2), p0, p1));
                        MessageAccessor.send(this.getLanguage(), sender, "region-add", id);
                    }
                    case "remove" -> {
                        var id = context.requireArgumentAt(1);

                        if (!service.existName(id)) {
                            MessageAccessor.send(this.getLanguage(), sender, "region-add-failed", id);
                            return;
                        }

                        service.delete(id);

                        MessageAccessor.send(this.getLanguage(), sender, "region-remove", id);
                    }

                    default -> {
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, "add", "remove", "list");
            suggestion.matchArgument(0, "add", (s) -> s.suggest(1, "[name]"));
            suggestion.matchArgument(0, "remove", (s) -> {
                try {
                    s.suggest(1, getModule().service.listNames());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
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

    @AutoRegister(Registers.BUKKIT_EVENT)
    public static final class BlockExplosionListener extends ModuleComponent<ExplosionDefender> {

        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("org.bukkit.event.block.BlockExplodeEvent"));
        }

        @EventHandler
        public void onBlockExplode(BlockExplodeEvent event) {
            var b = event.getBlock();
            if (this.parent.matchRegion(b.getLocation())) {
                return;
            }
            event.setCancelled(true);
            this.parent.handle(b.getLocation(), "[?]");
        }
    }
}