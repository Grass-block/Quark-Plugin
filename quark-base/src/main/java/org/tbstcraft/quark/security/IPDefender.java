package org.tbstcraft.quark.security;

import com.google.gson.JsonObject;
import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import net.kyori.adventure.util.TriState;
import org.bukkit.BanList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.tbstcraft.quark.api.DelayedPlayerJoinEvent;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.foundation.command.CommandExecutor;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.util.NetworkUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.3.4", recordFormat = {"Time", "Player", "OldIP", "NewIP"})
@CommandProvider(IPDefender.IPQueryCommand.class)
public final class IPDefender extends PackageModule implements CommandExecutor {
    @SuppressWarnings("HttpUrlsUsage")//yeah, because ip-api doesn't support https. fuck!!!!!
    public static final String API = "http://ip-api.com/json/%s?lang=%s";

    @Inject
    private LanguageEntry language;


    public static String defaultResult(Locale locale) {
        if (List.of(Locale.CHINESE, Locale.SIMPLIFIED_CHINESE, Locale.TRADITIONAL_CHINESE, Locale.CHINA).contains(locale)) {
            return "[未知]";
        }
        return "unknown";
    }

    public static String query(InetSocketAddress address, Locale locale) {
        if (address == null) {
            return defaultResult(locale);
        }

        String ipString = address.toString().replace("/", "").split(":")[0];

        String s;
        try {
            s = NetworkUtil.httpGet(API.formatted(ipString, locale.toString().replace("_", "-")), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (s.isEmpty()) {
            return defaultResult(locale);
        }

        try {

            JsonObject json = SharedObjects.JSON_PARSER.parse(s).getAsJsonObject();

            String result = "%s-%s-%s".formatted(
                    json.getAsJsonObject().get("country").getAsString(),
                    json.getAsJsonObject().get("regionName").getAsString(),
                    json.getAsJsonObject().get("city").getAsString()
            );
            if (Objects.equals(result, "null-null-null")) {
                return defaultResult(locale);
            }
            return result;
        } catch (Exception ignored) {
            return defaultResult(locale);
        }
    }

    public String query(Player player) {
        return query(player.getAddress(), Language.locale(player));
    }

    @EventHandler
    public void onPlayerJoin(DelayedPlayerJoinEvent event) {
        TaskService.asyncTask(() -> this.handle(event.getPlayer()));
    }

    public void handle(Player player) {
        TriState state;

        String current = query(player.getAddress(), Locale.ENGLISH);
        String previous;

        NBTTagCompound tag = PlayerDataService.getEntry(player.getName(), this.getId());
        if (!tag.hasKey("ip")) {
            previous = null;
            tag.setString("ip", current);
            PlayerDataService.save(player.getName());
            state = TriState.NOT_SET;
        } else {
            previous = tag.getString("ip");
            if (Objects.equals(previous, current)) {
                state = TriState.FALSE;
            } else {
                tag.setString("ip", current);
                PlayerDataService.save((player.getName()));
                state = TriState.TRUE;
            }
        }

        if (state == TriState.FALSE) {
            return;
        }

        String currentDisplay = query(player);
        if (state == TriState.NOT_SET) {
            this.language.sendMessage(player, "detect", currentDisplay);
            return;
        }

        this.language.sendMessage(player, "warn", currentDisplay);

        PluginMessenger.broadcastMapped("ip:change", (map) -> map
                .put("player", player.getName())
                .put("old-ip", previous)
                .put("new-ip", currentDisplay));

        if (this.getConfig().getBoolean("auto_ban")) {
            String name = player.getName();
            String reason = getLanguage().getMessage(Language.locale(player), "auto_ban_reason");
            int day = getConfig().getInt("auto_ban_day_time");
            int hour = getConfig().getInt("auto_ban_hour_time");
            int minute = getConfig().getInt("auto_ban_minute_time");
            int second = getConfig().getInt("auto_ban_second_time");

            Calendar calendar = Calendar.getInstance();

            calendar.add(Calendar.DATE, day);
            calendar.add(Calendar.HOUR, hour);
            calendar.add(Calendar.MINUTE, minute);
            calendar.add(Calendar.SECOND, second);

            PlayerUtil.banPlayer(name, BanList.Type.NAME, reason, calendar.getTime(), Quark.PLUGIN_ID);
        }

        if (this.getConfig().getBoolean("record")) {
            this.getRecord().addLine(
                    SharedObjects.DATE_FORMAT.format(new Date()),
                    player.getName(),
                    previous,
                    current
            );
        }
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        TaskService.asyncTask(() -> this.language.sendMessage(sender, "check", query(((Player) sender))));
    }

    @QuarkCommand(name = "check-ip", permission = "+quark.ip.query", playerOnly = true)
    public static final class IPQueryCommand extends ModuleCommand<IPDefender> {
        @Override
        public void init(IPDefender module) {
            setExecutor(module);
        }
    }
}