package org.tbstcraft.quark.framework.config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.ProductInfo;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.util.ObjectStatus;
import org.tbstcraft.quark.framework.module.ModuleManager;
import org.tbstcraft.quark.internal.data.ModuleDataService;
import org.tbstcraft.quark.internal.data.PlayerDataService;
import org.tbstcraft.quark.util.Utility;
import org.tbstcraft.quark.util.api.BukkitUtil;
import org.tbstcraft.quark.util.api.PlayerUtil;
import org.tbstcraft.quark.util.query.*;

import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public interface Queries {
    TemplateEngine GLOBAL_TEMPLATE_ENGINE = new TemplateEngine();
    ObjectiveTemplateEngine<Player> PLAYER_TEMPLATE_ENGINE = new ObjectiveTemplateEngine<>();
    GlobalVars EXTERNAL_VARS = new GlobalVars(GLOBAL_TEMPLATE_ENGINE);

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
        handler.register("black", new ValueSupplier(ChatColor.BLACK));
        handler.register("dark-blue", new ValueSupplier(ChatColor.DARK_BLUE));
        handler.register("dark-green", new ValueSupplier(ChatColor.DARK_GREEN));
        handler.register("dark-aqua", new ValueSupplier(ChatColor.DARK_AQUA));
        handler.register("dark-red", new ValueSupplier(ChatColor.DARK_RED));
        handler.register("dark-purple", new ValueSupplier(ChatColor.DARK_PURPLE));
        handler.register("gold", new ValueSupplier(ChatColor.GOLD));
        handler.register("gray", new ValueSupplier(ChatColor.GRAY));
        handler.register("dark-gray", new ValueSupplier(ChatColor.DARK_GRAY));
        handler.register("blue", new ValueSupplier(ChatColor.BLUE));
        handler.register("green", new ValueSupplier(ChatColor.GREEN));
        handler.register("aqua", new ValueSupplier(ChatColor.AQUA));
        handler.register("red", new ValueSupplier(ChatColor.RED));
        handler.register("purple", new ValueSupplier(ChatColor.LIGHT_PURPLE));
        handler.register("light-purple", new ValueSupplier(ChatColor.LIGHT_PURPLE));
        handler.register("yellow", new ValueSupplier(ChatColor.YELLOW));
        handler.register("white", new ValueSupplier(ChatColor.WHITE));

        handler.register("magic", new ValueSupplier(ChatColor.MAGIC));
        handler.register("bold", new ValueSupplier(ChatColor.BOLD));
        handler.register("delete", new ValueSupplier(ChatColor.STRIKETHROUGH));
        handler.register("underline", new ValueSupplier(ChatColor.UNDERLINE));
        handler.register("italic", new ValueSupplier(ChatColor.ITALIC));
        handler.register("reset", new ValueSupplier(ChatColor.RESET));

        handler.register("return", new ValueSupplier("\n"));
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
        handler.register("quark_framework_version",new ValueSupplier(ProductInfo.apiVersion()));
        handler.register("build_time",new ValueSupplier(ProductInfo.METADATA.getProperty("build-time")));
    }

    static void playerQueries(ObjectiveQueryHandler<Player> handler) {
        handler.register("name", Player::getName);
        handler.register("display_name", Player::getDisplayName);
        handler.register("custom_name", Player::getCustomName);
        handler.register("address", (p) -> Objects.requireNonNull(p.getAddress()).getAddress().getHostAddress());
        handler.register("locale", Player::getLocale);
        handler.register("ping", (p) -> BukkitUtil.formatPing(PlayerUtil.getPing(p)));
        handler.register("play_time", (p) -> Utility.formatDuring(PlayerUtil.getPlayTime(p)));
        handler.register("world_time", (p) -> {
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
