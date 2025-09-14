package org.atcraftmc.starlight.warp;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import net.kyori.adventure.text.event.ClickEvent;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.assertion.NumberLimitation;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.qlib.platform.PluginPlatform;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.data.WaypointService;
import org.atcraftmc.starlight.core.objects.Waypoint;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.StarlightCommandManager;
import org.atcraftmc.starlight.foundation.platform.Players;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.util.BukkitSound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@QuarkCommand(name = "waypoint")
@SLModule(version = "2.0.4")
@CommandProvider({Waypoints.WaypointCommand.class})
@AutoRegister({ServiceType.EVENT_LISTEN, ServiceType.CLIENT_MESSAGE})
public final class Waypoints extends CommandModule {
    private final SetHomeCommand setHomeCommand = new SetHomeCommand(this);
    private final WarpHomeCommand warpHomeCommand = new WarpHomeCommand(this);

    @Inject("-starlight.waypoint.public")
    private Permission editPublicPermission;

    @Inject("-starlight.waypoint.bypass")
    private Permission bypassAddLimitPermission;

    @Inject("-starlight.waypoint.admin")
    private Permission adminPermission;

    @Inject("tip")
    private LanguageItem tip;

    @Inject("tip-home")
    private LanguageItem tipHome;

    @Inject("starlight:default/sl_waypoints")
    private WaypointService service;

    @Override
    public void enable() throws Exception {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));

        if (ConfigAccessor.getBool(this.getConfig(), "home")) {
            StarlightCommandManager.getInstance().register(this.setHomeCommand);
            StarlightCommandManager.getInstance().register(this.warpHomeCommand);

            PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tipHome));
        }
    }

    @Override
    public void disable() throws Exception {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));

        if (ConfigAccessor.getBool(this.getConfig(), "home")) {
            StarlightCommandManager.getInstance().unregister(this.setHomeCommand);
            StarlightCommandManager.getInstance().unregister(this.warpHomeCommand);

            PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tipHome));
        }
    }

    public void teleport(Player player, String name) {
        var lang = getLanguage();

        try {
            if (!this.service.existName(name)) {
                lang.item("not-exist").send(player, name);
                return;
            }

            if (!this.service.hasAccess(player.getUniqueId(), name)) {
                lang.item("no-access").send(player, name);
                return;
            }

            var wp = this.service.byName(name).orElseThrow();
            var location = new Location(Bukkit.getWorld(wp.getWorld()), wp.getX(), wp.getY(), wp.getZ());
            location.setYaw(wp.getYaw());
            location.setPitch(wp.getPitch());

            Players.teleport(player, location).thenAccept((b) -> {
                BukkitSound.WARP.play(player);
                getLanguage().item("tp-success").send(player, name);
            });

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void add(CommandExecution context) {
        var name = context.requireArgumentAt(1);
        var lang = getLanguage();
        try {
            if (this.service.existName(name)) {
                lang.item("exist").send(context.getSender(), name);
                return;
            }

            var player = context.requireSenderAsPlayer();
            var uuid = UUID.randomUUID();
            var owner = player.getUniqueId();

            Waypoint wp;

            if (Objects.equals(context.requireArgumentAt(2), "#here")) {
                var world = player.getWorld().getName();
                var x = player.getLocation().getX();
                var y = player.getLocation().getY();
                var z = player.getLocation().getZ();
                var yaw = player.getLocation().getYaw();
                var pitch = player.getLocation().getPitch();

                wp = new Waypoint(uuid, name, world, x, y, z, yaw, pitch, owner, Set.of());
            } else {
                var x = context.requireArgumentDouble(3, NumberLimitation.any());
                var y = context.requireArgumentDouble(4, NumberLimitation.any());
                var z = context.requireArgumentDouble(5, NumberLimitation.any());
                var yaw = 0f;
                var pitch = 0f;


                if (context.hasArgumentAt(6)) {
                    yaw = context.requireArgumentFloat(6, NumberLimitation.any());
                    pitch = context.requireArgumentFloat(7, NumberLimitation.any());
                }

                var world = context.requireEnum(2, Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toSet()));

                wp = new Waypoint(uuid, name, world, x, y, z, yaw, pitch, owner, Set.of());
            }

            this.service.add(wp);

            lang.item("add-success").send(context.getSender(), name);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(CommandExecution context) {
        var lang = getLanguage();
        var name = context.requireArgumentAt(1);
        var player = context.requireSenderAsPlayer();

        try {
            if (!this.service.existName(name)) {
                lang.item("not-exist").send(player, name);
                return;
            }
            if (!this.service.hasControl(player.getUniqueId(), name) && !player.hasPermission(this.adminPermission)) {
                lang.item("remove-failed").send(player, name);
                return;
            }

            this.service.delete(name);
            lang.item("remove").send(player, name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void list(CommandExecution context) {
        var sender = context.requireSenderAsPlayer();
        var locale = LocaleService.locale(sender);
        var list = this.getLanguage().item("list").component(locale);

        try {
            this.service.listAccessible(sender.getUniqueId()).stream().filter((w) -> !w.getName().endsWith("#home")).forEach((w) -> {
                var name = w.getName();
                var owner = w.getOwner();
                var uuid = w.getUuid();
                var component = getLanguage().item("list-item")
                        .component(locale, name, w.getWorld(), ((int) w.getX()), ((int) w.getY()), ((int) w.getZ()), name);
                var hover = getLanguage().item("list-hover")
                        .component(locale, name, w.getWorld(), w.getX(), w.getY(), w.getZ(), w.getYaw(), w.getPitch(), owner, uuid);
                var line = component.asComponent()
                        .hoverEvent(hover.asComponent().asHoverEvent())
                        .clickEvent(ClickEvent.runCommand("/waypoint tp %s".formatted(name)));

                list.add(line);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        sender.sendMessage(PluginPlatform.global().globalFormatMessage("{#line}"));
        TextSender.sendMessage(sender, list);
        sender.sendMessage(PluginPlatform.global().globalFormatMessage("{#line}"));
    }

    private Optional<Waypoint> getForEdit(CommandExecution context, String name, Player player, UUID owner) throws SQLException {
        var lang = getLanguage();

        if (!this.service.existName(name)) {
            lang.item("not-exist").send(context.getSender(), name);
            return Optional.empty();
        }
        if (!this.service.hasControl(owner, name) && !player.hasPermission(this.adminPermission)) {
            lang.item("no-permission").send(context.getSender(), name);
            return Optional.empty();
        }

        return this.service.byName(name);
    }

    private void allow(CommandExecution context, String data) {
        var name = context.requireArgumentAt(1);
        var lang = getLanguage();
        try {
            var player = context.requireSenderAsPlayer();
            var owner = player.getUniqueId();

            this.getForEdit(context, name, player, owner).ifPresent((wp) -> {
                var d1 = data == null ? context.requireArgumentAt(2) : data;

                if (!Objects.equals(d1, "all")) {
                    var p = Objects.requireNonNull(context.requireOfflinePlayer(2));
                    wp.getAllowed().add(Objects.requireNonNull(p.getUniqueId()).toString());
                    lang.item("allow").send(context.getSender(), p.getName(), name);
                } else {
                    wp.getAllowed().add("all");
                    lang.item("allow-all").send(context.getSender(), name);
                }

                try {
                    this.service.update(wp);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void disallow(CommandExecution context) {
        var name = context.requireArgumentAt(1);
        var player = context.requireSenderAsPlayer();
        var owner = player.getUniqueId();
        var lang = getLanguage();
        try {
            this.getForEdit(context, name, player, owner).ifPresent((wp) -> {
                if (!Objects.equals(context.requireArgumentAt(2), "all")) {
                    var p = Objects.requireNonNull(context.requireOfflinePlayer(2));
                    wp.getAllowed().remove(Objects.requireNonNull(p.getUniqueId()).toString());
                    lang.item("disallow").send(context.getSender(), p.getName(), name);
                } else {
                    wp.getAllowed().remove("all");
                    lang.item("disallow-all").send(context.getSender(), name);
                }

                try {
                    this.service.update(wp);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @QuarkCommand(name = "waypoint", playerOnly = true, permission = "+starlight.waypoint.command")
    public static final class WaypointCommand extends ModuleCommand<Waypoints> {

        @Override
        public void execute(CommandExecution context) {
            var mode = context.requireEnum(0, "add", "remove", "list", "tp", "allow", "disallow", "add-public");

            switch (mode) {
                case "tp" -> getModule().teleport(context.requireSenderAsPlayer(), context.requireArgumentAt(1));
                case "add" -> getModule().add(context);
                case "list" -> getModule().list(context);
                case "remove" -> getModule().delete(context);
                case "allow" -> getModule().allow(context, null);
                case "disallow" -> getModule().disallow(context);
                case "add-public" -> {
                    context.requirePermission(getModule().editPublicPermission);
                    getModule().add(context);
                    getModule().allow(context, "all");
                }
            }
        }

        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, "tp", "list", "add", "remove", "allow", "disallow", "add-public");
            var player = suggestion.getSenderAsPlayer();

            suggestion.matchArgument(0, "add", s -> {
                s.suggest(1, "[name]");
                s.suggest(2, "#here");
                s.requireAnyPermission((ctx1) -> {
                    s.suggest(2, Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toSet()));
                    s.suggest(3, String.valueOf(player.getLocation().getX()));
                    s.suggest(4, String.valueOf(player.getLocation().getY()));
                    s.suggest(5, String.valueOf(player.getLocation().getZ()));
                    s.suggest(6, String.valueOf(player.getLocation().getYaw()));
                    s.suggest(7, String.valueOf(player.getLocation().getPitch()));
                }, getModule().bypassAddLimitPermission);
            });

            Consumer<CommandSuggestion> list = (ctx) -> {
                try {
                    ctx.suggest(
                            1,
                            getModule().service.listNameAccessible(suggestion.getSenderAsPlayer().getUniqueId())
                                    .stream()
                                    .filter((s) -> !s.contains("#home"))
                                    .collect(Collectors.toSet())
                    );
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            };

            suggestion.matchArgument(0, "remove", list);
            suggestion.matchArgument(0, "tp", list);
            suggestion.matchArgument(0, "allow", list);
            suggestion.matchArgument(0, "allow", (ctx) -> ctx.suggestPlayers(2));
            suggestion.matchArgument(0, "disallow", list);
            suggestion.matchArgument(0, "disallow", (ctx) -> {
                if (ctx.getBuffer().size() < 2) {
                    return;
                }
                try {
                    getModule().service.byName(ctx.getBuffer().get(1)).ifPresent((w) -> ctx.suggest(2, w.getAllowed().stream().map((s) -> {
                        if (s.contains("-")) {
                            return Bukkit.getOfflinePlayer(UUID.fromString(s)).getName();
                        } else {
                            return s;
                        }
                    }).collect(Collectors.toSet())));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @QuarkCommand(name = "sethome", playerOnly = true, permission = "+starlight.waypoint.sethome")
    public static final class SetHomeCommand extends ModuleCommand<Waypoints> {
        public SetHomeCommand(Waypoints waypoint) {
            super(waypoint);
            this.init();
        }

        private static @NotNull Waypoint dispatchWaypoint(CommandExecution context, String id) {
            var player = context.requireSenderAsPlayer();
            var uuid = player.getUniqueId();
            var x = player.getLocation().getX();
            var y = player.getLocation().getY();
            var z = player.getLocation().getZ();
            var yaw = player.getLocation().getYaw();
            var pitch = player.getLocation().getPitch();
            var world = player.getWorld().getName();

            var wp = new Waypoint(uuid, id, world, x, y, z, yaw, pitch, uuid, Set.of());
            return wp;
        }

        @Override
        public void execute(CommandExecution context) {
            try {
                var service = getModule().service;
                var id = context.requireSenderAsPlayer().getUniqueId() + "#home";

                if (service.existName(id) && context.getArgs().length == 0) {
                    MessageAccessor.send(this.getLanguage(), context.getSender(), "home-exist-warn");
                    return;
                }

                service.delete(id);

                var wp = dispatchWaypoint(context, id);

                service.add(wp);

                MessageAccessor.send(this.getLanguage(), context.getSender(), "home-set-success");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @QuarkCommand(name = "home", playerOnly = true, permission = "+starlight.waypoint.home")
    public static final class WarpHomeCommand extends ModuleCommand<Waypoints> {
        public WarpHomeCommand(Waypoints waypoint) {
            super(waypoint);
            this.init();
        }

        @Override
        public void execute(CommandExecution context) {
            try {
                var service = getModule().service;
                var id = context.requireSenderAsPlayer().getUniqueId() + "#home";
                var player = context.requireSenderAsPlayer();

                if (!service.existName(id)) {
                    MessageAccessor.send(this.getLanguage(), context.getSender(), "home-not-set");
                    return;
                }

                var wp = service.byName(id).orElseThrow();
                var location = new Location(Bukkit.getWorld(wp.getWorld()), wp.getX(), wp.getY(), wp.getZ());
                location.setYaw(wp.getYaw());
                location.setPitch(wp.getPitch());

                Players.teleport(player, location).thenAccept((b) -> {
                    BukkitSound.WARP.play(player);
                    MessageAccessor.send(this.getLanguage(), context.getSender(), "home-tp-success");
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
