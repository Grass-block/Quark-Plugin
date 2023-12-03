package org.tbstcraft.quark.security;

import com.google.gson.JsonParser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.service.PlayerDataService;
import org.tbstcraft.quark.util.BukkitUtil;
import org.tbstcraft.quark.util.NetworkUtil;
import org.tbstcraft.quark.util.nbt.NBTTagCompound;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@QuarkModule
public final class IPDefender extends PluginModule {

    @Override
    public void onEnable() {
        this.registerListener();
    }

    @Override
    public void onDisable() {
        this.unregisterListener();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new Thread(() -> this.handle(event.getPlayer())).start();
    }

    public void handle(Player player) {
        String ipLoc;
        try {
            String s = NetworkUtil.httpGet("http://ip-api.com/json/%s?lang=en-US".formatted(
                    Objects.requireNonNull(player.getAddress()).toString().replace("/", "")
                            .split(":")[0]));
            ipLoc = "%s-%s-%s".formatted(
                    new JsonParser().parse(s).getAsJsonObject().get("country"),
                    new JsonParser().parse(s).getAsJsonObject().get("regionName"),
                    new JsonParser().parse(s).getAsJsonObject().get("city")
            );
        } catch (IOException e) {
            return;
        }

        NBTTagCompound tag = PlayerDataService.getEntry(player.getName());
        if (!tag.hasKey("ip")) {
            this.getLanguage().sendMessageTo(player, "new_ip", ipLoc);
            tag.setString("ip", ipLoc);
            PlayerDataService.save(player.getName());
            return;
        }

        String oldIP = tag.getString("ip");
        if (Objects.equals(oldIP, ipLoc)) {
            return;
        }

        this.getLanguage().sendMessageTo(player, "ip_warn", ipLoc, oldIP);
        tag.setString("ip", ipLoc);
        PlayerDataService.save((player.getName()));

        if (this.getConfig().getBoolean("auto_ban")) {
            BukkitUtil.banPlayer(player.getName(),
                    getConfig().getInt("auto_ban_day_time"),
                    getConfig().getInt("auto_ban_hour_time"),
                    getConfig().getInt("auto_ban_minute_time"),
                    getConfig().getInt("auto_ban_second_time"),
                    getLanguage().getMessage(player.getLocale(), "auto_ban_reason"));
        }

        if (this.getConfig().getBoolean("record")) {
            this.getRecordEntry().record("[%s] player=%s oldIP=%s newIP=%s".formatted(
                    new SimpleDateFormat().format(new Date()), player.getName(), oldIP, ipLoc));
        }
    }
}