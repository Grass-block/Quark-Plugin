package org.tbstcraft.quark.defense;

import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.tbstcraft.quark.SharedContext;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.service.PlayerDataService;
import org.tbstcraft.quark.util.BukkitUtil;
import org.tbstcraft.quark.util.NetworkUtil;
import org.tbstcraft.quark.util.nbt.NBTTagCompound;
import org.tbstcraft.quark.util.registry.TypeItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@TypeItem("quark_defense:ip_defender")
public final class IPDefender extends PluginModule {
    private final CommandHandler command = new CommandHandler(this);

    @Override
    public void onEnable() {
        this.registerListener();
        this.registerCommand(this.command);
    }

    @Override
    public void onDisable() {
        this.unregisterCommand(this.command);
        this.unregisterListener();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SharedContext.SHARED_THREAD_POOL.submit(() -> {
            String ipLoc;
            try {
                String s = NetworkUtil.httpGet("http://ip-api.com/json/%s?lang=en-US".formatted(
                        Objects.requireNonNull(
                                        player.getAddress()).toString().replace("/", "")
                                .split(":")[0])
                );
                ipLoc = "%s-%s-%s".formatted(
                        new JsonParser().parse(s).getAsJsonObject().get("country"),
                        new JsonParser().parse(s).getAsJsonObject().get("regionName"),
                        new JsonParser().parse(s).getAsJsonObject().get("city")
                );
            } catch (IOException e) {
                return;
            }

            NBTTagCompound tag = PlayerDataService.getEntry(player.getName(),this.getId());

            if (!tag.hasKey("ip")) {
                this.getLanguage().sendMessageTo(player, "new_ip", ipLoc);
                tag.setString("ip", ipLoc);
                PlayerDataService.reload((player.getName()));
                return;
            }

            String oldIP = tag.getString("ip");


            if (Objects.equals(oldIP, ipLoc)) {
                return;
            }

            this.getLanguage().sendMessageTo(player, "ip_warn", ipLoc, oldIP);
            tag.setString("ip", ipLoc);
            PlayerDataService.reload((player.getName()));

            if (getConfig().isEnabled(this.getId(), "auto_ban")) {
                BukkitUtil.banPlayer(player.getName(),
                        getConfig().getInt("auto_ban_day_time", this.getId()),
                        getConfig().getInt("auto_ban_hour_time", this.getId()),
                        getConfig().getInt("auto_ban_minute_time", this.getId()),
                        getConfig().getInt("auto_ban_second_time", this.getId()),
                        getLanguage().getMessage(Locale.forLanguageTag(player.getLocale()), this.getId(), "auto_ban_reason"));
            }

            if (getConfig().isEnabled(this.getId(), "record")) {
                this.getRecordEntry().record("player=%s oldIP=%s newIP=%s", player.getName(), oldIP, ipLoc);
            }
        });
    }

    @QuarkCommand(name = "ip-defender", op = true)
    public static final class CommandHandler extends ModuleCommand<IPDefender> {
        public CommandHandler(IPDefender module) {
            super(module);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            PluginModule m = this.getModule();
            String arg2 = args[1];
            switch (args[0]) {
                case "auto-ban-time" -> {
                    this.checkException(args.length == 5);
                    m.getConfig().set("auto_ban_day_time", args[1]);
                    m.getConfig().set("auto_ban_hour_time", args[2]);
                    m.getConfig().set("auto_ban_minute_time", args[3]);
                    m.getConfig().set("auto_ban_second_time", args[4]);
                    m.getLanguage().sendMessageTo(sender, "command_auto_ban_time_set", args[1], args[2], args[3], args[4]);
                    m.getConfig().reload();
                }
                case "auto-ban" -> {
                    this.checkException(args.length == 2);
                    this.checkException(isBooleanOption(arg2));
                    m.getConfig().set("auto_ban", arg2);
                    m.getLanguage().sendMessageTo(sender, "command_auto_ban_set", arg2);
                }
                case "record" -> {
                    this.checkException(args.length == 2);
                    this.checkException(isBooleanOption(arg2));
                    m.getConfig().set("record", arg2);
                    m.getLanguage().sendMessageTo(sender, "command_record_set", arg2);
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
            if (args.length == 1) {
                tabList.add("auto-ban");
                tabList.add("auto-ban-time");
                tabList.add("record");
                return;
            }

            switch (args[0]) {
                case "auto-ban", "record" -> {
                    if (args.length + 1 > 2) {
                        return;
                    }
                    tabList.add("true");
                    tabList.add("false");
                }
                case "auto-ban-time" -> {
                    if (args.length > 5) {
                        return;
                    }
                    tabList.add("0");
                    tabList.add("1");
                    tabList.add("2");
                }
            }
        }

        @Override
        public @NotNull String getUsage() {
            return """
                    以下是各个子命令(蓝色填入整数, 黄色填入名称, 绿色填入tab指定的选项):
                    {white} - 自动封禁: /ip-defender {green}auto-ban [operation]
                    {white} - 封禁时间: /ip-defender {green}auto-ban-time {aqua}[day] [hour] [minute] [second]
                    {white} - 记录开关: /ip-defender {green}record [operation]
                     """;
        }

        @Override
        public @NotNull String getDescription() {
            return "对IP防护进行指定的操作。";
        }
    }
}