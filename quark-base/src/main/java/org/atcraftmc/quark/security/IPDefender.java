package org.atcraftmc.quark.security;

import com.google.gson.JsonObject;
import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.TriState;
import me.gb2022.commons.http.HttpMethod;
import me.gb2022.commons.http.HttpRequest;
import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.BanList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.data.language.LocaleMapping;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommandExecutor;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.framework.record.RecordEntry;
import org.tbstcraft.quark.internal.task.TaskService;

import java.net.InetSocketAddress;
import java.util.*;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.3.4")
@CommandProvider(IPDefender.IPQueryCommand.class)
public final class IPDefender extends PackageModule implements QuarkCommandExecutor {

    @Inject
    private LanguageEntry language;

    @Inject("ip-defender;Time,Player,OldIP,NewIP")
    private RecordEntry record;

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

        if (LocaleMapping.minecraft(Locale.getDefault()).contains("zh")) {

            var s = HttpRequest.https(HttpMethod.GET, "searchplugin.csdn.net/api/v1/ip/get")
                    .browserBehavior(false)
                    .param("ip", ipString)
                    .build()
                    .request();

            JsonObject json = SharedObjects.JSON_PARSER.parse(s).getAsJsonObject();

            return json.getAsJsonObject().getAsJsonObject("data").get("address").getAsString();

        } else {
            var loc = locale.toString().replace("_", "-");

            var s = HttpRequest.http(HttpMethod.GET, "ip-api.com/json")
                    .path(ipString)
                    .param("lang", loc)
                    .browserBehavior(false)
                    .build()
                    .request();

            JsonObject json = SharedObjects.JSON_PARSER.parse(s).getAsJsonObject();

            String result = "%s-%s-%s".formatted(json.getAsJsonObject().get("country").getAsString(),
                                                 json.getAsJsonObject().get("regionName").getAsString(),
                                                 json.getAsJsonObject().get("city").getAsString()
                                                );
            if (Objects.equals(result, "null-null-null")) {
                return defaultResult(locale);
            }
            return result;
        }

    }

    public String query(Player player) {
        return query(player.getAddress(), Language.locale(player));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        TaskService.async().run(() -> this.handle(event.getPlayer()));
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
            state = TriState.UNKNOWN;
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
        if (state == TriState.UNKNOWN) {
            this.language.sendMessage(player, "detect", currentDisplay);
            return;
        }

        this.language.sendMessage(player, "warn", currentDisplay);

        PluginMessenger.broadcastMapped("ip:change",
                                        (map) -> map.put("player", player.getName()).put("old-ip", previous).put("new-ip", currentDisplay)
                                       );

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

            Players.banPlayer(name, BanList.Type.NAME, reason, calendar.getTime(), Quark.PLUGIN_ID);
        }

        if (this.getConfig().getBoolean("record")) {
            this.record.addLine(SharedObjects.DATE_FORMAT.format(new Date()), player.getName(), previous, current);
        }
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        TaskService.async().run(() -> this.language.sendMessage(sender, "check", query(((Player) sender))));
    }

    @QuarkCommand(name = "check-ip", permission = "+quark.ip.query", playerOnly = true)
    public static final class IPQueryCommand extends ModuleCommand<IPDefender> {
        @Override
        public void init(IPDefender module) {
            setExecutor(module);
        }
    }
}