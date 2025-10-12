package org.atcraftmc.starlight.display;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.api.event.BanMessageFetchEvent;
import org.atcraftmc.starlight.api.event.KickMessageFetchEvent;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.BanList;
import org.bukkit.event.EventHandler;

import java.util.Date;
import java.util.Objects;

@AutoRegister({Registrations.SERVER_EVENT, Registrations.PLUGIN_MESSAGE})
@ApplicationModule(id = "custom-kick-message", version = "1.0.0")
public final class CustomKickMessage extends SLPackageModule {

    @Inject
    private LanguageEntry language;


    @EventHandler
    public void onBanMessageQuery(BanMessageFetchEvent event) {
        event.setResultMessage(buildBanUI(
                event.getType(),
                event.getReason(),
                event.getExpires(),
                event.getTarget(),
                event.getSource(),
                event.getLocale()
        ));
    }

    @EventHandler
    public void onKickMessageQuery(KickMessageFetchEvent event) {
        var msg = MessageAccessor.buildTemplate(
                this.language,
                LocaleService.locale(event.getPlayer()),
                Language.generateTemplate(this.config(), "ui")
        );

        event.setResultMessage(msg.replace("{reason}", event.getReason()));
    }


    public String buildBanUI(BanList.Type type, String reason, Date expiration, String target, String source, MinecraftLocale locale) {
        String msg;
        if (type == BanList.Type.NAME) {
            msg = MessageAccessor.buildTemplate(
                    this.language,
                    locale,
                    Language.generateTemplate(this.config(), "ban-ui", (s) -> s.replace("@type", "ban-name"))
            );
        } else {
            msg = MessageAccessor.buildTemplate(
                    this.language,
                    locale,
                    Language.generateTemplate(this.config(), "ban-ui", (s) -> s.replace("@type", "ban-ip"))
            );
        }

        if (expiration == null) {
            msg = msg.replace("{expire}", "(forever)");
        } else {
            msg = msg.replace("{expire}", SharedObjects.DATE_FORMAT.format(expiration));
        }

        msg = msg.replace("{reason}", Objects.requireNonNullElse(reason, "(no reason)"));
        msg = msg.replace("{name}", target).replace("{source}", source);

        return msg;
    }
}
