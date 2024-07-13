package org.atcraftmc.quark.web;

import com.google.gson.JsonParser;
import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.BanList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.util.NetworkUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.3.4", recordFormat = {"Time", "Player", "OldIP", "NewIP"})
public final class IPDefender extends PackageModule {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        TaskService.asyncTask(() -> this.handle(event.getPlayer()));
    }

    public void handle(Player player) {
        String ipLoc;
        try {
            String s = NetworkUtil.httpGet("http://ip-api.com/json/%s?lang=en-US".formatted(
                    Objects.requireNonNull(player.getAddress()).toString().replace("/", "")
                            .split(":")[0]), false);
            ipLoc = "%s-%s-%s".formatted(
                    new JsonParser().parse(s).getAsJsonObject().get("country"),
                    new JsonParser().parse(s).getAsJsonObject().get("regionName"),
                    new JsonParser().parse(s).getAsJsonObject().get("city")
            );
        } catch (IOException e) {
            return;
        }

        NBTTagCompound tag = PlayerDataService.getEntry(player.getName(), this.getId());
        if (!tag.hasKey("ip")) {
            this.getLanguage().sendMessage(player, "new_ip", ipLoc);
            tag.setString("ip", ipLoc);
            PlayerDataService.save(player.getName());
            return;
        }

        String oldIP = tag.getString("ip");
        if (Objects.equals(oldIP, ipLoc)) {
            return;
        }

        this.getLanguage().sendMessage(player, "ip_warn", ipLoc, oldIP);

        PluginMessenger.broadcastMapped("ip:change", (map) -> map
                .put("player", player.getName())
                .put("old-ip", oldIP)
                .put("new-ip", ipLoc));

        tag.setString("ip", ipLoc);
        PlayerDataService.save((player.getName()));

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
                    oldIP,
                    ipLoc
            );
        }
    }
}