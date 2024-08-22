package org.tbstcraft.quark.contents;

import com.google.gson.JsonArray;
import me.gb2022.apm.client.event.ClientRequestEvent;
import me.gb2022.apm.client.event.driver.ClientEventHandler;
import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.generator.WorldInfo;
import org.bukkit.permissions.Permission;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.data.ModuleDataService;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.*;
import org.tbstcraft.quark.foundation.command.assertion.NumberLimitation;
import org.tbstcraft.quark.foundation.command.execute.CommandSuggestion;
import org.tbstcraft.quark.foundation.platform.APIProfile;
import org.tbstcraft.quark.foundation.platform.BukkitCodec;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.util.BukkitSound;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@QuarkCommand(name = "waypoint")
@QuarkModule(version = "2.0.3", compatBlackList = {APIProfile.ARCLIGHT})
@CommandProvider({Waypoint.WaypointCommand.class})
@AutoRegister({ServiceType.EVENT_LISTEN, ServiceType.CLIENT_MESSAGE})
public final class Waypoint extends CommandModule {
    private final Map<String, Location> deathPoints = new HashMap<>();

    @Inject("-quark.waypoint.public")
    private Permission editPublicPermission;

    @Inject("-quark.waypoint.bypass")
    private Permission bypassAddLimitPermission;

    @Inject("tip")
    private LanguageItem tip;

    @Inject("tip-home")
    private LanguageItem tipHome;

    @Inject("tip-back")
    private LanguageItem tipBack;

    @ClientEventHandler("/quark/waypoint/list-private")
    public void onWaypointFetchPrivate(ClientRequestEvent event) {
        JsonArray array = new JsonArray();

        NBTTagCompound entry = PlayerDataService.getEntry(event.getPlayer(), this.getId());

        for (String s : entry.getTagMap().keySet()) {
            array.add(s);
        }
        event.makeResponse(array);
    }

    @ClientEventHandler("/quark/waypoint/list")
    public void onWaypointFetch(ClientRequestEvent event) {
        JsonArray array = new JsonArray();
        NBTTagCompound entry = ModuleDataService.getEntry(this.getId());
        for (String s : entry.getTagMap().keySet()) {
            array.add(s);
        }
        event.makeResponse(array);
    }


    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));

        if (this.getConfig().getBoolean("home")) {
            CommandManager.registerQuarkCommand(new SetHomeCommand(this));
            CommandManager.registerQuarkCommand(new WarpHomeCommand(this));

            PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tipHome));
        }
        if (this.getConfig().getBoolean("back-to-death")) {
            CommandManager.registerQuarkCommand(new BackToDeathCommand(this));

            PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tipBack));
        }
    }

    @Override
    public void disable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));

        if (this.getConfig().getBoolean("home")) {
            CommandManager.unregister("sethome");
            CommandManager.unregister("home");

            PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tipHome));
        }
        if (this.getConfig().getBoolean("back-to-death")) {
            CommandManager.unregister("back");

            PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tipBack));
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        this.getLanguage().sendMessage(event.getPlayer(), "back-hint");
    }

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent event) {
        this.deathPoints.put(event.getPlayer().getName(), event.getPlayer().getLocation());
    }

    @QuarkCommand(name = "waypoint", playerOnly = true, permission = "+quark.waypoint.command")
    public static final class WaypointCommand extends ModuleCommand<Waypoint> {

        @Override
        public void execute(org.tbstcraft.quark.foundation.command.execute.CommandExecution context) {
            var mode = context.requireEnum(0, "add-private", "remove-private", "tp-private", "list-private", "add", "remove", "tp", "list");
            var isPrivate = mode.contains("private");
            var sender = context.requireSenderAsPlayer();

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
                    StringBuilder sb = new StringBuilder(128);
                    entry.getTagMap().forEach((name, data) -> sb.append(name).append(ChatColor.GRAY).append(" -> ").append(ChatColor.WHITE).append(BukkitCodec.string(BukkitCodec.location(entry.getCompoundTag(name)))).append("\n"));
                    this.getLanguage().sendMessage(sender, "list", sb);
                }
                case "tp", "tp-private" -> {
                    var id = context.requireArgumentAt(1);

                    NBTTagCompound tag = entry.getCompoundTag(id);
                    if (tag == null || !tag.hasKey("world")) {
                        this.getLanguage().sendMessage(sender, "not-exist", id);
                        break;
                    }

                    Players.teleport(sender, BukkitCodec.location(tag));
                    BukkitSound.WARP.play(sender);
                    this.getLanguage().sendMessage(sender, "tp-success", id);
                }
                case "add", "add-private" -> {
                    var id = context.requireArgumentAt(1);
                    var tag = entry.getCompoundTag(id);

                    if (tag.hasKey("world")) {
                        this.getLanguage().sendMessage(sender, "exist", id);
                        return;
                    }

                    Location loc;

                    if (!context.hasArgumentAt(2) || Objects.equals(context.requireArgumentAt(2), "@self")) {
                        loc = sender.getLocation();
                    } else {

                        if (!this.getConfig().getBoolean("allow-coordinate-add")) {
                            context.requirePermission(this.getModule().bypassAddLimitPermission);
                        }

                        loc = new Location(Bukkit.getWorld(context.requireEnum(2, Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toSet()))), context.requireArgumentDouble(3, NumberLimitation.any()), context.requireArgumentDouble(4, NumberLimitation.any()), context.requireArgumentDouble(5, NumberLimitation.any()));
                        if (context.hasArgumentAt(6)) {
                            loc.setYaw(context.requireArgumentFloat(6, NumberLimitation.any()));
                            loc.setPitch(context.requireArgumentFloat(7, NumberLimitation.any()));
                        }
                    }

                    tag = BukkitCodec.nbt(loc);
                    entry.setCompoundTag(id, tag);
                    this.getLanguage().sendMessage(sender, "add-success", id);

                    PlayerDataService.save(sender.getName());
                    ModuleDataService.save(this.getModuleId());
                }
                case "remove", "remove-private" -> {
                    var id = context.requireArgumentAt(1);
                    var tag = entry.getCompoundTag(id);

                    if (!tag.hasKey("world")) {
                        this.getLanguage().sendMessage(sender, "not-exist", id);
                        return;
                    }
                    entry.remove(id);
                    this.getLanguage().sendMessage(sender, "remove-success", id);
                }
            }
        }

        @Override
        public void suggest(org.tbstcraft.quark.foundation.command.execute.CommandSuggestion suggestion) {
            suggestion.suggest(0, "tp", "tp-private", "list", "list-private", "add-private", "remove-private");
            suggestion.requireAnyPermission((ctx) -> ctx.suggest(0, "add", "remove"), this.getModule().editPublicPermission);

            var player = suggestion.getSenderAsPlayer();
            Consumer<org.tbstcraft.quark.foundation.command.execute.CommandSuggestion> add = suggestion1 -> {
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

            Consumer<org.tbstcraft.quark.foundation.command.execute.CommandSuggestion> privateList = (ctx) -> {
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
                this.getLanguage().sendMessage(sender, "home-exist-warn");
                return;
            }
            entry.setCompoundTag("home", BukkitCodec.nbt(((Player) sender).getLocation()));
            this.getLanguage().sendMessage(sender, "home-set-success");
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
                this.getLanguage().sendMessage(sender, "home-not-set");
                return;
            }
            Location loc = BukkitCodec.location(entry.getCompoundTag("home"));
            Players.teleport(((Player) sender), loc);
            this.getLanguage().sendMessage(sender, "home-tp-success");
            BukkitSound.WARP.play((Player) sender);
        }
    }

    @QuarkCommand(name = "back", playerOnly = true, permission = "+quark.warp.back")
    public static final class BackToDeathCommand extends ModuleCommand<Waypoint> {
        public BackToDeathCommand(Waypoint waypoint) {
            super(waypoint);
            this.init();
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (!this.getModule().deathPoints.containsKey(sender.getName())) {
                this.getLanguage().sendMessage(sender, "back-not-set");
                return;
            }
            Players.teleport(((Player) sender), this.getModule().deathPoints.get(sender.getName()));
            this.getLanguage().sendMessage(sender, "back-tp-success");
            BukkitSound.WARP.play(((Player) sender));
        }
    }
}
