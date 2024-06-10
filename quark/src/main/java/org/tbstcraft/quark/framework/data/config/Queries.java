package org.tbstcraft.quark.framework.data.config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.ProductInfo;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.framework.data.language.Language;
import org.tbstcraft.quark.framework.module.ModuleManager;
import org.tbstcraft.quark.internal.data.ModuleDataService;
import org.tbstcraft.quark.internal.data.PlayerDataService;
import org.tbstcraft.quark.util.ObjectStatus;
import org.tbstcraft.quark.util.Utility;
import org.tbstcraft.quark.util.platform.BukkitUtil;
import org.tbstcraft.quark.util.platform.PlayerUtil;
import org.tbstcraft.quark.util.query.ObjectiveQueryHandler;
import org.tbstcraft.quark.util.query.ObjectiveTemplateEngine;
import org.tbstcraft.quark.util.query.QueryHandler;
import org.tbstcraft.quark.util.query.TemplateEngine;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public interface Queries {
    TemplateEngine GLOBAL_TEMPLATE_ENGINE = new TemplateEngine();
    ObjectiveTemplateEngine<Player> PLAYER_TEMPLATE_ENGINE = new ObjectiveTemplateEngine<>();
    GlobalVars EXTERNAL_VARS = new GlobalVars(GLOBAL_TEMPLATE_ENGINE);

    Map<String, String> ENVIRONMENT_VARS = new HashMap<>();
    Pattern ENV_PATTERN = Pattern.compile("\\{\\$(.*?)}");

    static void setEnvironmentVars(ConfigurationSection section) {
        ENVIRONMENT_VARS.clear();
        for (String s : section.getKeys(false)) {
            ENVIRONMENT_VARS.put(s, section.getString(s));
        }
    }

    static String applyEnvironmentVars(String input) {
        List<String> result = new ArrayList<>();
        Matcher matcher = ENV_PATTERN.matcher(input);
        while (matcher.find()) {
            result.add(matcher.group());
        }

        for (String s : result) {
            String s2 = s.substring(2, s.length() - 1);
            String replacement = ENVIRONMENT_VARS.get(s2);
            if (replacement == null) {
                continue;
            }
            input = input.replace(s, replacement);
        }
        return input;
    }


    static void reloadExternal() {
        globalVars();
    }

    static void initialize() {
        QueryHandler global = GLOBAL_TEMPLATE_ENGINE.getQueryHandler();
        serverQueries(global);
        chatComponents(global);
        quarkStats(global);
        playerQueries(PLAYER_TEMPLATE_ENGINE.getQueryHandler());
    }

    static void chatComponents(QueryHandler handler) {
        handler.register("black", () -> ChatColor.BLACK);
        handler.register("dark-blue", () -> ChatColor.DARK_BLUE);
        handler.register("dark-green", () -> ChatColor.DARK_GREEN);
        handler.register("dark-aqua", () -> ChatColor.DARK_AQUA);
        handler.register("dark-red", () -> ChatColor.DARK_RED);
        handler.register("dark-purple", () -> ChatColor.DARK_PURPLE);
        handler.register("gold", () -> ChatColor.GOLD);
        handler.register("gray", () -> ChatColor.GRAY);
        handler.register("dark-gray", () -> ChatColor.DARK_GRAY);
        handler.register("blue", () -> ChatColor.BLUE);
        handler.register("green", () -> ChatColor.GREEN);
        handler.register("aqua", () -> ChatColor.AQUA);
        handler.register("red", () -> ChatColor.RED);
        handler.register("purple", () -> ChatColor.LIGHT_PURPLE);
        handler.register("light-purple", () -> ChatColor.LIGHT_PURPLE);
        handler.register("yellow", () -> ChatColor.YELLOW);
        handler.register("white", () -> ChatColor.WHITE);

        handler.register("magic", () -> ChatColor.MAGIC);
        handler.register("bold", () -> ChatColor.BOLD);
        handler.register("delete", () -> ChatColor.STRIKETHROUGH);
        handler.register("underline", () -> ChatColor.UNDERLINE);
        handler.register("italic", () -> ChatColor.ITALIC);
        handler.register("reset", () -> ChatColor.RESET);

        handler.register("return", () -> "\n");
        handler.register("date", () -> SharedObjects.DATE_FORMAT.format(new Date()));
    }

    static void globalVars() {
        EXTERNAL_VARS.setHolder(Quark.PLUGIN);
        EXTERNAL_VARS.load();
    }

    static void quarkStats(QueryHandler handler) {
        handler.register("module_installed", () -> ModuleManager.getAllModules().size());
        handler.register("module_enabled", () -> ModuleManager.getByStatus(ObjectStatus.ENABLED).size());
        handler.register("player_data_count", PlayerDataService::getEntryCount);
        handler.register("module_data_count", ModuleDataService::getEntryCount);
        handler.register("quark_version", ProductInfo::version);
        handler.register("quark_framework_version", ProductInfo::apiVersion);
        handler.register("build_time", () -> ProductInfo.METADATA.getProperty("build-time"));
    }

    static void playerQueries(ObjectiveQueryHandler<Player> handler) {
        handler.register("name", Player::getName);
        handler.register("display-name", Player::getDisplayName);
        handler.register("custom-name", Player::getName);
        handler.register("address", (p) -> Objects.requireNonNull(p.getAddress()).getAddress().getHostAddress());
        handler.register("locale", (p) -> Language.locale(Language.locale(p)));
        handler.register("ping", (p) -> BukkitUtil.formatPing(PlayerUtil.getPing(p)));
        handler.register("play-time", (p) -> Utility.formatDuring(PlayerUtil.getPlayTime(p)));
        handler.register("world-time", (p) -> {
            int time = (int) p.getWorld().getTime() - 18000;

            if (time < 0) {
                time = 24000 + time;
            }
            int hour = time / 1000;
            int min = (int) ((time + 19000) % 1000 * (60 / 1000f));
            return String.format("%02d:%02d", hour, min);
        });
    }

    static void serverQueries(QueryHandler handler) {
        handler.register("name", (ServerAdapter) Server::getName);
        handler.register("max_player", (ServerAdapter) Server::getMaxPlayers);
        handler.register("bukkit_version", (ServerAdapter) Server::getBukkitVersion);
        handler.register("player", () -> String.valueOf(Bukkit.getOnlinePlayers().size()));
        handler.register("tps", () -> BukkitUtil.formatTPS(BukkitUtil.getTPS()));
        handler.register("mspt", () -> BukkitUtil.formatMSPT(BukkitUtil.getMSPT()));
    }


    interface ServerAdapter extends Supplier<Object> {
        @Override
        default String get() {
            return this.get(Bukkit.getServer()).toString();
        }

        Object get(Server server);
    }
}
