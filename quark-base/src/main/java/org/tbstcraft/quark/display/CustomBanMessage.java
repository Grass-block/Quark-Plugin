package org.tbstcraft.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.framework.data.language.Language;
import org.tbstcraft.quark.framework.event.BanMessageFetchEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.Locale;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.0.2")
public final class CustomBanMessage extends PackageModule {

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH)
    public void onConnect(AsyncPlayerPreLoginEvent event) {
        Locale locale = Locale.CHINA;
        String player = event.getName();
        OfflinePlayer p = Bukkit.getOfflinePlayer(player);
        if (p.getPlayer() != null) {
            locale = Language.locale(p.getPlayer());
        }

        String ui;

        if (Bukkit.getBanList(BanList.Type.NAME).isBanned(player)) {
            BanEntry entry = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(player);
            if (entry == null) {
                return;
            }
            ui = this.buildBanUI(entry, BanList.Type.NAME, locale);
        } else if (Bukkit.getBanList(BanList.Type.IP).isBanned(player)) {
            BanEntry entry = Bukkit.getBanList(BanList.Type.IP).getBanEntry(player);
            if (entry == null) {
                return;
            }
            ui = this.buildBanUI(entry, BanList.Type.IP, locale);
        } else {
            return;
        }

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ui);
    }

    public String buildBanUI(BanEntry entry, BanList.Type type, Locale locale) {
        String msg;
        if (type == BanList.Type.NAME) {
            msg = this.getLanguage().buildTemplate(locale, Language.generateTemplate(this.getConfig(), "ui",
                    (s) -> s.replace("@type", "name")));
        } else {
            msg = this.getLanguage().buildTemplate(locale, Language.generateTemplate(this.getConfig(), "ui",
                    (s) -> s.replace("@type", "ip")));
        }

        if (entry.getExpiration() == null) {
            msg = msg.replace("{expire}", "(forever)");
        } else {
            msg = msg.replace("{expire}", SharedObjects.DATE_FORMAT.format(entry.getExpiration()));
        }

        if (entry.getReason() != null) {
            msg = msg.replace("{reason}", entry.getReason());
        } else {
            msg = msg.replace("{reason}", "(no reason)");
        }

        msg = msg.replace("{name}", entry.getTarget()).replace("{source}", entry.getSource());

        return msg;
    }

    @EventHandler
    public void onBanMessageFetch(BanMessageFetchEvent event) {
        event.setMessage(buildBanUI(event.getEntry(), event.getType(), Language.locale(event.getLocale())));
    }
}
