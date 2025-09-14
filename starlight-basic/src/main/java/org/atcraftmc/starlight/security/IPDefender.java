package org.atcraftmc.starlight.security;

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
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.starlight.ProductInfo;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.ui.TextRenderer;
import org.atcraftmc.starlight.data.PlayerDataService;
import org.atcraftmc.starlight.data.record.RecordService;
import org.atcraftmc.starlight.data.record.registry.DataRenderer;
import org.atcraftmc.starlight.data.record.registry.RecordField;
import org.atcraftmc.starlight.data.record.registry.RecordRegistry;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.foundation.platform.Players;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.BanList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

@AutoRegister(ServiceType.EVENT_LISTEN)
@SLModule(version = "1.3.4")
@CommandProvider(IPDefender.IPQueryCommand.class)
public final class IPDefender extends PackageModule implements PluginCommandExecutor {
    private static final RecordRegistry.A3<Player, String, String> RECORD = new RecordRegistry.A3<>(
            "ip-log",
            new RecordField<>("player", TextRenderer.literal("Player"), DataRenderer.PLAYER),
            new RecordField<>("old-ip", TextRenderer.literal("Old-IP"), DataRenderer.STRING),
            new RecordField<>("new-ip", TextRenderer.literal("Current-IP"), DataRenderer.STRING)
    );

    @Inject
    private LanguageEntry language;

    private IPService service;

    public static String defaultResult(MinecraftLocale locale) {
        if (List.of(MinecraftLocale.ZH_CN, MinecraftLocale.ZH_HK, MinecraftLocale.ZH_TW, MinecraftLocale.LZH).contains(locale)) {
            return "[未知]";
        }
        return "unknown";
    }

    @Override
    public void enable() {
        this.service = switch (getConfig().value("service").string()) {
            case "ip-api" -> new IPService.IP_API();
            case "pconline" -> new IPService.PConLine();
            case "baidu" -> new IPService.Baidu();
            case "real-ip" -> new IPService.RealIP();
            default -> {
                getL4jLogger().error("unknown service: {},using default ip-api service", getConfig().value("service").string());
                yield new IPService.IP_API();
            }
        };
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        TaskService.async().run(() -> MessageAccessor.send(this.language, sender, "check", query(((Player) sender))));
    }

    public void check(Player player) {
        var current = query(player.getAddress(), MinecraftLocale.EN_US);
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
            MessageAccessor.send(this.language, player, "detect", currentDisplay);
            return;
        }

        MessageAccessor.send(this.language, player, "warn", currentDisplay);

        PluginMessenger.broadcastMapped("ip:change", (map) -> {
            map.put("player", player.getName());
            map.put("old-ip", previous);
            map.put("new-ip", current);
        });

        if (ConfigAccessor.getBool(this.getConfig(), "record")) {
            RecordService.record(RECORD.render(player, previous, current));
        }

        if (ConfigAccessor.getBool(this.getConfig(), "auto_ban")) {
            var name = player.getName();
            var reason = getLanguage().item("auto_ban_reason").message(LocaleService.locale(player));

            var day = ConfigAccessor.getInt(getConfig(), "auto_ban_day_time");
            var hour = ConfigAccessor.getInt(getConfig(), "auto_ban_hour_time");
            var minute = ConfigAccessor.getInt(getConfig(), "auto_ban_minute_time");
            var second = ConfigAccessor.getInt(getConfig(), "auto_ban_second_time");

            var calendar = Calendar.getInstance();

            calendar.add(Calendar.DATE, day);
            calendar.add(Calendar.HOUR, hour);
            calendar.add(Calendar.MINUTE, minute);
            calendar.add(Calendar.SECOND, second);

            Players.banPlayer(name, BanList.Type.NAME, reason, calendar.getTime(), ProductInfo.CORE_ID);
        }
    }

    public String query(InetSocketAddress address, MinecraftLocale locale) {
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
        return query(player.getAddress(), LocaleService.locale(player));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        TaskService.async().run(() -> this.check(event.getPlayer()));
    }

    interface IPService {
        Logger LOGGER = LogManager.getLogger("Starlight-Plugin/IPService");

        String RES_UNKNOWN = "[error]Unknown or LAN address";
        String RES_NET_ERROR = "[error]Network error";
        String RES_UNEXPECTED_RESPONSE = "[error]Unexpected response from server";

        static boolean isError(String resp) {
            return resp.startsWith("[error]");
        }

        HttpRequest buildRequest(InetSocketAddress address, MinecraftLocale locale);

        String parse(JsonObject response);

        default String parse(InetSocketAddress address, MinecraftLocale locale) {
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
            public HttpRequest buildRequest(InetSocketAddress address, MinecraftLocale locale) {
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
            public HttpRequest buildRequest(InetSocketAddress address, MinecraftLocale locale) {
                return HttpRequest.http(HttpMethod.GET, "opendata.baidu.com/api.php")
                        .param("query", address.getHostName())
                        .param("co", "")
                        .param("resource_id", "6006")
                        .param("oe", "utf8")
                        .browserBehavior(true)
                        .build();
            }
        }

        class PConLine implements IPService {
            @Override
            public HttpRequest buildRequest(InetSocketAddress address, MinecraftLocale locale) {
                return HttpRequest.http(HttpMethod.GET, "whois.pconline.com.cn/ipJson.jsp")
                        .param("ip", address.toString())
                        .param("json", "true")
                        .build();
            }

            @Override
            public String parse(JsonObject response) {
                return response.get("addr").getAsString();
            }
        }

        class RealIP implements IPService {
            @Override
            public HttpRequest buildRequest(InetSocketAddress address, MinecraftLocale locale) {
                return HttpRequest.http(HttpMethod.GET, "realip.cc").param("ip", address.toString()).build();
            }

            @Override
            public String parse(JsonObject response) {
                return "";
            }
        }

    }

    @QuarkCommand(name = "check-ip", permission = "+starlight.ip.query", playerOnly = true)
    public static final class IPQueryCommand extends ModuleCommand<IPDefender> {
        @Override
        public void init(IPDefender module) {
            setExecutor(module);
        }
    }
}