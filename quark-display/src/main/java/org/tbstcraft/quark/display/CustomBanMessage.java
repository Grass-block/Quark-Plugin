package org.tbstcraft.quark.display;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;

import java.util.Objects;

@QuarkModule
public class CustomBanMessage extends PluginModule {

    @Override
    public void onEnable() {
        this.registerListener();
    }

    @Override
    public void onDisable() {
        this.unregisterListener();
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onConnect(AsyncPlayerPreLoginEvent event) {
        String locale = "zh_cn";
        OfflinePlayer p = Bukkit.getOfflinePlayer(event.getName());
        if (p.getPlayer() != null) {
            locale = p.getPlayer().getLocale();
        }


        if (Bukkit.getBanList(BanList.Type.NAME).isBanned(event.getName())) {
            BanEntry entry = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(event.getName());
            String msg;
            assert entry != null;
            if (entry.getExpiration() == null) {
                msg = this.getLanguage().getMessage(locale, "ban_name_permanently");
            } else {
                msg = this.getLanguage().getMessage(locale, "ban_name");
                msg = msg.replace("expire", SharedObjects.DATE_FORMAT.format(entry.getExpiration()));
            }

            msg = this.getLanguage().getMessage(locale, "global_header") + msg + this.getLanguage().getMessage(locale, "global_footer");

            msg = msg.replace("{name}", event.getName())
                    .replace("{source}", entry.getSource());

            if (entry.getReason() != null) {
                msg = msg.replace("{reason}", entry.getReason());
            } else {
                msg = msg.replace("{reason}", "(no reason)");
            }

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, msg);
            return;
        }
        if (Bukkit.getBanList(BanList.Type.IP).isBanned(event.getName())) {
            BanEntry entry = Bukkit.getBanList(BanList.Type.IP).getBanEntry(event.getName());

            String msg;
            assert entry != null;
            if (entry.getExpiration() == null) {
                msg = this.getLanguage().getMessage(locale, "ban_ip_permanently");
            } else {
                msg = this.getLanguage().getMessage(locale, "ban_ip");
                msg = msg.replace("expire", SharedObjects.DATE_FORMAT.format(entry.getExpiration()));
            }

            msg = this.getLanguage().getMessage(locale, "global_header") + msg + this.getLanguage().getMessage(locale, "global_footer");

            msg = msg.replace("{name}", event.getName())
                    .replace("{source}", entry.getSource());

            if (entry.getReason() != null) {
                msg = msg.replace("{reason}", entry.getReason());
            } else {
                msg = msg.replace("{reason}", "(no reason)");
            }

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, msg);
        }
    }
}
