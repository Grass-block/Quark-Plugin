package org.tbstcraft.quark.internal.placeholder;

import me.gb2022.commons.Formating;
import me.gb2022.commons.TriState;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.texts.placeholder.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.ProductInfo;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.ModuleDataService;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.module.ModuleManager;

import java.util.Date;
import java.util.Objects;

public interface PlaceHolders {
    static GloballyPlaceHolder chat() {
        GloballyPlaceHolder holder = new GloballyPlaceHolder();

        holder.register("&0", GlobalPlaceHolder.value(ChatColor.BLACK), "black");
        holder.register("&1", GlobalPlaceHolder.value(ChatColor.DARK_BLUE), "dark-blue", "dark_blue");
        holder.register("&2", GlobalPlaceHolder.value(ChatColor.DARK_GREEN), "dark-green", "dark_green");
        holder.register("&3", GlobalPlaceHolder.value(ChatColor.DARK_AQUA), "dark-aqua", "dark_aqua");
        holder.register("&4", GlobalPlaceHolder.value(ChatColor.DARK_RED), "dark-red", "dark_red");
        holder.register("&5", GlobalPlaceHolder.value(ChatColor.DARK_PURPLE), "dark-purple", "dark_purple");
        holder.register("&6", GlobalPlaceHolder.value(ChatColor.GOLD), "gold");
        holder.register("&7", GlobalPlaceHolder.value(ChatColor.GRAY), "gray", "light-gray", "light_gray");
        holder.register("&8", GlobalPlaceHolder.value(ChatColor.DARK_GRAY), "dark-gray", "dark_gray");
        holder.register("&9", GlobalPlaceHolder.value(ChatColor.BLUE), "blue");
        holder.register("&a", GlobalPlaceHolder.value(ChatColor.GREEN), "green", "light-green", "light_green");
        holder.register("&b", GlobalPlaceHolder.value(ChatColor.AQUA), "aqua", "light-blue", "light-aqua", "light_aqua", "light_blue");
        holder.register("&c", GlobalPlaceHolder.value(ChatColor.RED), "red", "light-red", "light_red");
        holder.register("&d", GlobalPlaceHolder.value(ChatColor.LIGHT_PURPLE), "purple", "light-purple", "light_purple");
        holder.register("&e", GlobalPlaceHolder.value(ChatColor.YELLOW), "yellow");
        holder.register("&f", GlobalPlaceHolder.value(ChatColor.WHITE), "white");

        holder.register("&k", GlobalPlaceHolder.value(ChatColor.MAGIC), "magic", "obfuscate");
        holder.register("&l", GlobalPlaceHolder.value(ChatColor.BOLD), "bold");
        holder.register("&m", GlobalPlaceHolder.value(ChatColor.STRIKETHROUGH), "delete", "strikethrough");
        holder.register("&n", GlobalPlaceHolder.value(ChatColor.UNDERLINE), "underline");
        holder.register("&o", GlobalPlaceHolder.value(ChatColor.ITALIC), "italic");
        holder.register("&r", GlobalPlaceHolder.value(ChatColor.RESET), "reset", "unset");

        holder.register("return", GlobalPlaceHolder.value("\n"));

        return holder;
    }

    static GloballyPlaceHolder server() {
        GloballyPlaceHolder holder = new GloballyPlaceHolder();

        holder.register("version", GlobalPlaceHolder.value(Bukkit.getServer().getVersion()), "server-version");
        holder.register("bukkit-version", GlobalPlaceHolder.value(Bukkit.getServer().getBukkitVersion()));
        holder.register("name", Bukkit.getServer().getName());
        holder.register("max-player", Bukkit.getServer().getMaxPlayers());
        holder.register("player", GlobalPlaceHolder.object(() -> Bukkit.getOnlinePlayers().size()));
        holder.register("tps", (StringPlaceHolder) () -> BukkitUtil.formatTPS(BukkitUtil.getTPS()));
        holder.register("mspt", (StringPlaceHolder) () -> BukkitUtil.formatMSPT(BukkitUtil.getMSPT()));

        holder.register("date", (StringPlaceHolder) () -> SharedObjects.DATE_FORMAT.format(new Date()), "date-full");
        holder.register("date-day", (StringPlaceHolder) () -> SharedObjects.DAY_FORMAT.format(new Date()), "day");
        holder.register("date-time", (StringPlaceHolder) () -> SharedObjects.TIME_FORMAT.format(new Date()), "time");

        return holder;
    }

    static ObjectivePlaceHolder<Player> player() {
        ObjectivePlaceHolder<Player> holder = new ObjectivePlaceHolder<>();
        holder.register("name", (StringObjectPlaceHolder<Player>) Player::getName);
        holder.register("display-name", (StringObjectPlaceHolder<Player>) Player::getDisplayName);
        holder.register("custom-name", (StringObjectPlaceHolder<Player>) Player::getName);
        holder.register(
                "address",
                (StringObjectPlaceHolder<Player>) (p) -> Objects.requireNonNull(p.getAddress()).getAddress().getHostAddress()
        );
        holder.register("locale", (StringObjectPlaceHolder<Player>) (p) -> Language.locale(Language.locale(p)));
        holder.register("ping", (StringObjectPlaceHolder<Player>) (p) -> BukkitUtil.formatPing(Players.getPing(p)));
        holder.register("play-time", (StringObjectPlaceHolder<Player>) (p) -> Formating.formatDuringFull(Players.getPlayTime(p)));
        holder.register("world-time", (StringObjectPlaceHolder<Player>) (p) -> {
            int time = (int) p.getWorld().getTime() - 18000;

            if (time < 0) {
                time = 24000 + time;
            }
            int hour = time / 1000;
            int min = (int) ((time + 19000) % 1000 * (60 / 1000f));
            return String.format("%02d:%02d", hour, min);
        });

        holder.register("ping-value", (StringObjectPlaceHolder<Player>) p -> String.valueOf(Players.getPing(p)));


        return holder;
    }

    static GloballyPlaceHolder quarkStats() {
        GloballyPlaceHolder holder = new GloballyPlaceHolder();

        holder.register("module-installed", GlobalPlaceHolder.object(() -> ModuleManager.getInstance().getModules().size()));
        holder.register(
                "module-enabled",
                GlobalPlaceHolder.object(() -> ModuleManager.getInstance().getIdsByStatus(TriState.FALSE).size())
        );
        holder.register("player-data-count", GlobalPlaceHolder.object(PlayerDataService::entryCount));
        holder.register("module-data-count", GlobalPlaceHolder.object(ModuleDataService::getEntryCount));
        holder.register("quark-version", (StringPlaceHolder) ProductInfo::version);
        holder.register("quark-framework_version", GlobalPlaceHolder.object(ProductInfo::apiVersion));
        holder.register("build-time", GlobalPlaceHolder.object(() -> ProductInfo.METADATA.getProperty("build-time")));

        return holder;
    }
}
