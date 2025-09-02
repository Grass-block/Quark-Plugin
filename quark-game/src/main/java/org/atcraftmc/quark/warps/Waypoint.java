package org.atcraftmc.quark.warps;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import net.kyori.adventure.text.event.ClickEvent;
import org.atcraftmc.qlib.platform.PluginPlatform;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.assertion.NumberLimitation;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.LanguageItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.permissions.Permission;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.starlight.data.ModuleDataService;
import org.atcraftmc.starlight.data.PlayerDataService;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.StarlightCommandManager;
import org.atcraftmc.starlight.foundation.platform.BukkitCodec;
import org.atcraftmc.starlight.foundation.platform.Players;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.util.BukkitSound;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@QuarkCommand(name = "waypoint")
@SLModule(version = "2.0.3")
@CommandProvider({Waypoint.WaypointCommand.class})
@AutoRegister({ServiceType.EVENT_LISTEN, ServiceType.CLIENT_MESSAGE})
public final class Waypoint extends CommandModule {
    private final SetHomeCommand setHomeCommand = new SetHomeCommand(this);
    private final WarpHomeCommand warpHomeCommand = new WarpHomeCommand(this);

    @Inject("-quark.waypoint.public")
    private Permission editPublicPermission;

    @Inject("-quark.waypoint.bypass")
    private Permission bypassAddLimitPermission;

    @Inject("tip")
    private LanguageItem tip;

    @Inject("tip-home")
    private LanguageItem tipHome;

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));

        if (ConfigAccessor.getBool(this.getConfig(), "home")) {
            StarlightCommandManager.getInstance().register(this.setHomeCommand);
            StarlightCommandManager.getInstance().register(this.warpHomeCommand);

            PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tipHome));
        }
    }

    @Override
    public void disable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));

        if (ConfigAccessor.getBool(this.getConfig(), "home")) {
            StarlightCommandManager.getInstance().unregister(this.setHomeCommand);
            StarlightCommandManager.getInstance().unregister(this.warpHomeCommand);

            PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tipHome));
        }
    }

    @QuarkCommand(name = "waypoint", playerOnly = true, permission = "+quark.waypoint.command")
    public static final class WaypointCommand extends ModuleCommand<Waypoint> {

        @Override
        public void execute(CommandExecution context) {
            var mode = context.requireEnum(0, "add-private", "remove-private", "tp-private", "list-private", "add", "remove", "tp", "list");
            var isPrivate = mode.contains("private");
            var sender = context.requireSenderAsPlayer();
            var locale = LocaleService.locale(sender);

            NBTTagCompound entry;
            if (isPrivate) {
                entry = PlayerDataService.getEntry(sender.getName(), getModule().getId());
            } else {
                entry = ModuleDataService.getEntry(this.getModuleId());
            }

            if (!isPrivate && !mode.contains("tp")) {
                context.requirePermission(this.getModule().editPublicPermission);
            }

            switch (mode) {
                case "list", "list-private" -> {
                    var list = this.getLanguage().item("list").component(locale);

                    entry.getTagMap().forEach((name, data) -> {
                        var loc = BukkitCodec.location(entry.getCompoundTag(name));
                        var component = getLanguage().item("list-item").component(
                                locale,
                                name,
                                loc.getWorld().getName(),
                                loc.getBlockX(),
                                loc.getBlockY(),
                                loc.getBlockZ(),
                                isPrivate ? "-private" : "",
                                name
                        );
                        var hover = getLanguage().item("list-hover")
                                .component(
                                        locale,
                                        name,
                                        loc.getWorld().getName(),
                                        loc.getX(),
                                        loc.getY(),
                                        loc.getZ(),
                                        loc.getYaw(),
                                        loc.getPitch()
                                );
                        var line = component.asComponent()
                                .hoverEvent(hover.asComponent().asHoverEvent())
                                .clickEvent(ClickEvent.runCommand("/waypoint %s %s".formatted(isPrivate ? "tp-private" : "tp", name)));

                        list.add(line);
                    });

                    sender.sendMessage(PluginPlatform.global().globalFormatMessage("{#line}"));
                    TextSender.sendMessage(sender, list);
                    sender.sendMessage(PluginPlatform.global().globalFormatMessage("{#line}"));
                }
                case "tp", "tp-private" -> {
                    var id = context.requireArgumentAt(1);

                    NBTTagCompound tag = entry.getCompoundTag(id);
                    if (tag == null || !tag.hasKey("world")) {
                        MessageAccessor.send(this.getLanguage(), sender, "not-exist", id);
                        break;
                    }

                    Players.teleport(sender, BukkitCodec.location(tag));
                    BukkitSound.WARP.play(sender);
                    MessageAccessor.send(this.getLanguage(), sender, "tp-success", id);
                }
                case "add", "add-private" -> {
                    var id = context.requireArgumentAt(1);
                    var tag = entry.getCompoundTag(id);

                    if (tag.hasKey("world")) {
                        MessageAccessor.send(this.getLanguage(), sender, "exist", id);
                        return;
                    }

                    Location loc;

                    if (!context.hasArgumentAt(2) || Objects.equals(context.requireArgumentAt(2), "@self")) {
                        loc = sender.getLocation();
                    } else {

                        if (!ConfigAccessor.getBool(this.getConfig(), "allow-coordinate-add")) {
                            context.requirePermission(this.getModule().bypassAddLimitPermission);
                        }

                        loc = new Location(
                                Bukkit.getWorld(context.requireEnum(
                                        2,
                                        Bukkit.getWorlds()
                                                .stream()
                                                .map(WorldInfo::getName)
                                                .collect(Collectors.toSet())
                                )),
                                context.requireArgumentDouble(3, NumberLimitation.any()),
                                context.requireArgumentDouble(4, NumberLimitation.any()),
                                context.requireArgumentDouble(5, NumberLimitation.any())
                        );
                        if (context.hasArgumentAt(6)) {
                            loc.setYaw(context.requireArgumentFloat(6, NumberLimitation.any()));
                            loc.setPitch(context.requireArgumentFloat(7, NumberLimitation.any()));
                        }
                    }

                    tag = BukkitCodec.nbt(loc);
                    entry.setCompoundTag(id, tag);
                    MessageAccessor.send(this.getLanguage(), sender, "add-success", id);

                    PlayerDataService.save(sender.getName());
                    ModuleDataService.save(this.getModuleId());
                }
                case "remove", "remove-private" -> {
                    var id = context.requireArgumentAt(1);
                    var tag = entry.getCompoundTag(id);

                    if (!tag.hasKey("world")) {
                        MessageAccessor.send(this.getLanguage(), sender, "not-exist", id);
                        return;
                    }
                    entry.remove(id);
                    MessageAccessor.send(this.getLanguage(), sender, "remove-success", id);
                }
            }
        }

        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, "tp", "tp-private", "list", "list-private", "add-private", "remove-private");
            suggestion.requireAnyPermission((ctx) -> ctx.suggest(0, "add", "remove"), this.getModule().editPublicPermission);

            var player = suggestion.getSenderAsPlayer();
            Consumer<CommandSuggestion> add = suggestion1 -> {
                suggestion1.suggest(1, "[name]");
                suggestion1.suggest(2, "@self");
                suggestion1.requireAnyPermission((ctx) -> {
                    suggestion1.suggest(2, Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toSet()));
                    suggestion1.suggest(3, String.valueOf(player.getLocation().getX()));
                    suggestion1.suggest(4, String.valueOf(player.getLocation().getY()));
                    suggestion1.suggest(5, String.valueOf(player.getLocation().getZ()));
                    suggestion1.suggest(6, String.valueOf(player.getLocation().getYaw()));
                    suggestion1.suggest(7, String.valueOf(player.getLocation().getPitch()));
                }, getModule().bypassAddLimitPermission);
            };

            suggestion.matchArgument(0, "add", add);
            suggestion.matchArgument(0, "add-private", add);

            Consumer<CommandSuggestion> privateList = (ctx) -> {
                var map = PlayerDataService.getEntry(player.getName(), this.getModuleId()).getTagMap();
                ctx.suggest(1, map.keySet());
            };
            Consumer<CommandSuggestion> pubList = (ctx) -> {
                var map = ModuleDataService.getEntry(this.getModuleId()).getTagMap();
                ctx.suggest(1, map.keySet());
            };

            suggestion.matchArgument(0, "remove", pubList);
            suggestion.matchArgument(0, "tp", pubList);
            suggestion.matchArgument(0, "remove-private", privateList);
            suggestion.matchArgument(0, "tp-private", privateList);
        }
    }

    @QuarkCommand(name = "sethome", playerOnly = true, permission = "+quark.warp.sethome")
    public static final class SetHomeCommand extends ModuleCommand<Waypoint> {
        public SetHomeCommand(Waypoint waypoint) {
            super(waypoint);
            this.init();
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            NBTTagCompound entry = PlayerDataService.getEntry(sender.getName(), this.getModuleId());
            if (entry.hasKey("home") && (args.length < 1 || !Objects.equals(args[0], "-f"))) {
                MessageAccessor.send(this.getLanguage(), sender, "home-exist-warn");
                return;
            }
            entry.setCompoundTag("home", BukkitCodec.nbt(((Player) sender).getLocation()));
            MessageAccessor.send(this.getLanguage(), sender, "home-set-success");
        }
    }

    @QuarkCommand(name = "home", playerOnly = true, permission = "+quark.warp.tphome")
    public static final class WarpHomeCommand extends ModuleCommand<Waypoint> {
        public WarpHomeCommand(Waypoint waypoint) {
            super(waypoint);
            this.init();
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            NBTTagCompound entry = PlayerDataService.getEntry(sender.getName(), this.getModuleId());
            if (!entry.hasKey("home")) {
                MessageAccessor.send(this.getLanguage(), sender, "home-not-set");
                return;
            }
            Location loc = BukkitCodec.location(entry.getCompoundTag("home"));
            Players.teleport(((Player) sender), loc);
            MessageAccessor.send(this.getLanguage(), sender, "home-tp-success");
            BukkitSound.WARP.play((Player) sender);
        }
    }

}
