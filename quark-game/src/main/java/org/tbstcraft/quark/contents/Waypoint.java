package org.tbstcraft.quark.contents;

import com.google.gson.JsonArray;
import me.gb2022.apm.client.event.ClientRequestEvent;
import me.gb2022.apm.client.event.driver.ClientEventHandler;
import me.gb2022.commons.nbt.NBTBase;
import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.tbstcraft.quark.framework.command.CommandManager;
import org.tbstcraft.quark.framework.command.CommandProvider;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import me.gb2022.commons.reflect.AutoRegister;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.data.ModuleDataService;
import org.tbstcraft.quark.internal.data.PlayerDataService;
import org.tbstcraft.quark.util.BukkitSound;
import org.tbstcraft.quark.util.platform.APIProfile;
import org.tbstcraft.quark.util.platform.BukkitCodec;
import org.tbstcraft.quark.util.platform.PlayerUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@QuarkCommand(name = "waypoint")
@QuarkModule(version = "2.0.3", compatBlackList = {APIProfile.ARCLIGHT})
@CommandProvider({Waypoint.WaypointCommand.class})
@AutoRegister({ServiceType.EVENT_LISTEN,ServiceType.CLIENT_MESSAGE})
public final class Waypoint extends CommandModule {
    private final Map<String, Location> deathPoints = new HashMap<>();

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
        if (this.getConfig().getBoolean("home")) {
            CommandManager.registerCommand(new SetHomeCommand(this));
            CommandManager.registerCommand(new WarpHomeCommand(this));
        }
        if (this.getConfig().getBoolean("back-to-death")) {
            CommandManager.registerCommand(new BackToDeathCommand(this));
        }
    }

    @Override
    public void disable() {
        if (this.getConfig().getBoolean("home")) {
            CommandManager.unregisterCommand("sethome");
            CommandManager.unregisterCommand("home");
        }
        if (this.getConfig().getBoolean("back-to-death")) {
            CommandManager.unregisterCommand("back");
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
        public void onCommand(CommandSender sender, String[] args) {
            String id = args.length >= 2 ? args[1] : null;

            NBTTagCompound entry;
            if (args[0].contains("-private")) {
                entry = PlayerDataService.getEntry(sender.getName(), this.getModule().getId());
            } else {
                entry = ModuleDataService.getEntry(this.getModule().getId());
            }

            switch (args[0]) {
                case "list", "list-private" -> {
                    StringBuilder sb = new StringBuilder(128);
                    entry.getTagMap().forEach((name, data) -> sb.append(name)
                            .append(ChatColor.GRAY)
                            .append(" -> ")
                            .append(ChatColor.WHITE)
                            .append(BukkitCodec.toString(BukkitCodec.fromNBT(entry.getCompoundTag(name))))
                            .append("\n"));
                    this.getLanguage().sendMessage(sender, "list", sb);
                    return;
                }
                case "tp", "tp-private" -> {
                    NBTTagCompound tag = entry.getCompoundTag(id);
                    if (tag == null) {
                        this.getLanguage().sendMessage(sender, "not-exist", id);
                        break;
                    }
                    if (sender instanceof Player p) {
                        PlayerUtil.teleport(p, BukkitCodec.fromNBT(tag));
                        BukkitSound.WARP.play(p);
                        this.getLanguage().sendMessage(sender, "tp-success", id);
                    }
                    return;
                }
            }

            if (!sender.isOp() && !args[0].contains("-private")) {
                this.sendPermissionMessage(sender);
                return;
            }

            NBTTagCompound tag = entry.getCompoundTag(id);

            switch (args[0]) {
                case "add", "add-private" -> {
                    if (tag.hasKey("world")) {
                        this.getLanguage().sendMessage(sender, "exist", id);
                        return;
                    }
                    if (args[2].equals("@self")) {
                        tag = BukkitCodec.toNBT(((Player) sender).getLocation());
                    } else {
                        if (!this.getConfig().getBoolean("allow-coordinate-add")
                                && !sender.isOp()
                                && Objects.equals(args[0], "add-private")) {
                            this.sendExceptionMessage(sender);
                            return;
                        }
                        Location loc = new Location(
                                Bukkit.getWorld(args[2]),
                                Double.parseDouble(args[3]),
                                Double.parseDouble(args[4]),
                                Double.parseDouble(args[5])
                        );
                        if (args.length >= 8) {
                            loc.setYaw(Float.parseFloat(args[6]));
                            loc.setPitch(Float.parseFloat(args[7]));
                        }

                        tag = BukkitCodec.toNBT(loc);
                    }
                    entry.setCompoundTag(id, tag);
                    this.getLanguage().sendMessage(sender, "add-success", id);
                }
                case "remove", "remove-private" -> {
                    if (!tag.hasKey("world")) {
                        this.getLanguage().sendMessage(sender, "not-exist", id);
                        return;
                    }
                    entry.remove(id);
                    this.getLanguage().sendMessage(sender, "remove-success", id);
                }
            }

            if (args[0].contains("-private")) {
                PlayerDataService.save(sender.getName());
            } else {
                ModuleDataService.save(this.getModule().getId());
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                if (sender.isOp()) {
                    tabList.add("add");
                    tabList.add("remove");
                }
                tabList.add("tp");
                tabList.add("list");
                tabList.add("tp-private");
                tabList.add("list-private");
                tabList.add("add-private");
                tabList.add("remove-private");
                return;
            }

            if (buffer[0].equals("add") || buffer[0].equals("add-private")) {
                if (buffer[0].equals("add-private") && buffer.length == 3) {
                    if (this.getConfig().getBoolean("allow_coordinate_add")) {
                        tabList.add("@self");
                        return;
                    }
                }
                switch (buffer.length) {
                    case 2 -> tabList.add("[name]");
                    case 3 -> {
                        Bukkit.getWorlds().forEach((w) -> tabList.add(w.getName()));
                        tabList.add("@self");
                    }
                    case 4 -> tabList.add("[x]");
                    case 5 -> tabList.add("[y]");
                    case 6 -> tabList.add("[z]");
                    case 7 -> tabList.add("(yaw)");
                    case 8 -> tabList.add("(pitch)");
                }
                return;
            }

            if (buffer.length == 2) {
                if (buffer[0].equals("list") || buffer[0].equals("list-private")) {
                    return;
                }

                Map<String, NBTBase> map;

                if (buffer[0].contains("-private")) {
                    map = PlayerDataService.getEntry(sender.getName(), this.getModule().getId()).getTagMap();
                } else {
                    map = ModuleDataService.getEntry(this.getModule().getId()).getTagMap();
                }
                tabList.addAll(map.keySet());
            }
        }
    }

    @QuarkCommand(name = "sethome", playerOnly = true, permission = "+quark.waypoint.sethome")
    public static final class SetHomeCommand extends ModuleCommand<Waypoint> {
        public SetHomeCommand(Waypoint waypoint) {
            super(waypoint);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            NBTTagCompound entry = PlayerDataService.getEntry(sender.getName(), this.getModuleId());
            if (entry.hasKey("home") && (args.length < 1 || !Objects.equals(args[0], "-f"))) {
                this.getLanguage().sendMessage(sender, "home-exist-warn");
                return;
            }
            entry.setCompoundTag("home", BukkitCodec.toNBT(((Player) sender).getLocation()));
            this.getLanguage().sendMessage(sender, "home-set-success");
        }
    }

    @QuarkCommand(name = "home", playerOnly = true, permission = "+quark.waypoint.tp-home")
    public static final class WarpHomeCommand extends ModuleCommand<Waypoint> {
        public WarpHomeCommand(Waypoint waypoint) {
            super(waypoint);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            NBTTagCompound entry = PlayerDataService.getEntry(sender.getName(), this.getModuleId());
            if (!entry.hasKey("home")) {
                this.getLanguage().sendMessage(sender, "home-not-set");
                return;
            }
            Location loc = BukkitCodec.fromNBT(entry.getCompoundTag("home"));
            PlayerUtil.teleport(((Player) sender), loc);
            this.getLanguage().sendMessage(sender, "home-tp-success");
            BukkitSound.WARP.play((Player) sender);
        }
    }

    @QuarkCommand(name = "back", playerOnly = true, permission = "+quark.waypoint.back")
    public static final class BackToDeathCommand extends ModuleCommand<Waypoint> {
        public BackToDeathCommand(Waypoint waypoint) {
            super(waypoint);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (!this.getModule().deathPoints.containsKey(sender.getName())) {
                this.getLanguage().sendMessage(sender, "back-not-set");
                return;
            }
            PlayerUtil.teleport(((Player) sender), this.getModule().deathPoints.get(sender.getName()));
            this.getLanguage().sendMessage(sender, "back-tp-success");
            BukkitSound.WARP.play(((Player) sender));
        }
    }
}
