package org.atcraftmc.quark.security;

import com.google.gson.JsonObject;
import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.TriState;
import me.gb2022.commons.http.HttpMethod;
import me.gb2022.commons.http.HttpRequest;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.BanList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.PlayerDataService;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.language.LanguageEntry;
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

    private IPService service;

    public static String defaultResult(Locale locale) {
        if (List.of(Locale.CHINESE, Locale.SIMPLIFIED_CHINESE, Locale.TRADITIONAL_CHINESE, Locale.CHINA).contains(locale)) {
            return "[未知]";
        }
        return "unknown";
    }

    @Override
    public void enable() {
        this.service = switch (getConfig().getString("service")) {
            case "ip-api" -> new IPService.IP_API();
            case "ua-info" -> new IPService.UserAgentInfo();
            case "baidu" -> new IPService.Baidu();
            default -> {
                getL4jLogger().error("unknown service: {},using default ip-api service", getConfig().getString("service"));
                yield new IPService.IP_API();
            }
        };
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        TaskService.async().run(() -> this.language.sendMessage(sender, "check", query(((Player) sender))));
    }

    public void check(Player player) {
        var current = query(player.getAddress(), Locale.ENGLISH);
        var data = PlayerDataService.get(player);

        TriState state;
        String previous;

        if (!data.hasKey("ip-address")) {
            data.setString("ip-address", current);
            data.save();

            state = TriState.UNKNOWN;
            previous = null;
        } else {
            previous = data.getString("ip-address");

            if (Objects.equals(previous, current)) {
                state = TriState.FALSE;
            } else {
                data.setString("ip-address", current);
                data.save();

                state = TriState.TRUE;
            }
        }

        if (state == TriState.FALSE) {
            return;
        }

        var currentDisplay = query(player);

        if (state == TriState.UNKNOWN) {
            this.language.sendMessage(player, "detect", currentDisplay);
            return;
        }

        this.language.sendMessage(player, "warn", currentDisplay);

        PluginMessenger.broadcastMapped("ip:change", (map) -> {
            map.put("player", player.getName());
            map.put("old-ip", previous);
            map.put("new-ip", current);
        });

        if (this.getConfig().getBoolean("record")) {
            this.record.addLine(SharedObjects.DATE_FORMAT.format(new Date()), player.getName(), previous, current);
        }

        if (this.getConfig().getBoolean("auto_ban")) {
            var name = player.getName();
            var reason = getLanguage().getMessage(Language.locale(player), "auto_ban_reason");

            var day = getConfig().getInt("auto_ban_day_time");
            var hour = getConfig().getInt("auto_ban_hour_time");
            var minute = getConfig().getInt("auto_ban_minute_time");
            var second = getConfig().getInt("auto_ban_second_time");

            var calendar = Calendar.getInstance();

            calendar.add(Calendar.DATE, day);
            calendar.add(Calendar.HOUR, hour);
            calendar.add(Calendar.MINUTE, minute);
            calendar.add(Calendar.SECOND, second);

            Players.banPlayer(name, BanList.Type.NAME, reason, calendar.getTime(), Quark.PLUGIN_ID);
        }
    }

    public String query(InetSocketAddress address, Locale locale) {
        if (address == null) {
            return defaultResult(locale);
        }

        var result = this.service.parse(address, locale);

        if (IPService.isError(result)) {
            return defaultResult(locale);
        }

        return result;
    }

    public String query(Player player) {
        return query(player.getAddress(), Language.locale(player));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        TaskService.async().run(() -> this.check(event.getPlayer()));
    }

    interface IPService {
        Logger LOGGER = LogManager.getLogger("Quark-Plugin/IPService");

        String RES_UNKNOWN = "[error]Unknown or LAN address";
        String RES_NET_ERROR = "[error]Network error";
        String RES_UNEXPECTED_RESPONSE = "[error]Unexpected response from server";

        static boolean isError(String resp) {
            return resp.startsWith("[error]");
        }

        HttpRequest buildRequest(InetSocketAddress address, Locale locale);

        String parse(JsonObject response);

        default String parse(InetSocketAddress address, Locale locale) {
            var resp = ((String) null);
            var dom = ((JsonObject) null);

            try {
                resp = buildRequest(address, locale).request();
            } catch (Exception e) {
                LOGGER.error(RES_NET_ERROR);
                LOGGER.catching(e);
                return RES_NET_ERROR;
            }

            try {
                dom = SharedObjects.JSON_PARSER.parse(resp).getAsJsonObject();
            } catch (Exception e) {
                LOGGER.error(RES_UNEXPECTED_RESPONSE);
                LOGGER.error(resp);
                LOGGER.catching(e);
                return RES_UNEXPECTED_RESPONSE;
            }

            return parse(dom);
        }


        class IP_API implements IPService {
            @Override
            public HttpRequest buildRequest(InetSocketAddress address, Locale locale) {
                return HttpRequest.http(HttpMethod.GET, "ip-api.com/json")
                        .path(address.getHostName())
                        .param("lang", locale.toString().replace("_", "-"))
                        .browserBehavior(false)
                        .build();
            }


            @Override
            public String parse(JsonObject response) {
                var country = response.get("country").getAsString();
                var regionName = response.get("regionName").getAsString();
                var city = response.get("city").getAsString();
                var result = "%s-%s-%s".formatted(country, regionName, city);

                if (Objects.equals(result, "null-null-null")) {
                    return RES_UNKNOWN;
                }
                return result;
            }
        }

        class Baidu implements IPService {
            @Override
            public String parse(JsonObject response) {
                if (response.getAsJsonArray("data").isEmpty()) {
                    return RES_UNKNOWN;
                }
                return response.getAsJsonArray("data").get(0).getAsJsonObject().get("location").getAsString();
            }

            @Override
            public HttpRequest buildRequest(InetSocketAddress address, Locale locale) {
                return HttpRequest.http(HttpMethod.GET, "opendata.baidu.com/api.php")
                        .param("query", address.getHostName())
                        .param("co", "")
                        .param("resource_id", "6006")
                        .param("oe", "utf8")
                        .browserBehavior(true)
                        .build();
            }
        }

        class UserAgentInfo implements IPService {

            @Override
            public HttpRequest buildRequest(InetSocketAddress address, Locale locale) {
                return HttpRequest.http(HttpMethod.GET, "ip.useragentinfo.com/json")
                        .param("ip", address.getHostName())
                        .browserBehavior(true)
                        .build();
            }

            @Override
            public String parse(JsonObject response) {
                var country = response.get("country").getAsString();
                var province = response.get("province").getAsString();
                var city = response.get("city").getAsString();

                if (response.has("desc") && Objects.equals(response.get("desc").getAsString(), "query fail")) {
                    return RES_UNKNOWN;
                }

                return "%s-%s-%s".formatted(country, province, city);
            }
        }
    }

    @QuarkCommand(name = "check-ip", permission = "+quark.ip.query", playerOnly = true)
    public static final class IPQueryCommand extends ModuleCommand<IPDefender> {
        @Override
        public void init(IPDefender module) {
            setExecutor(module);
        }
    }
}