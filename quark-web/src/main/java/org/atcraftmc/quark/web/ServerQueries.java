package org.atcraftmc.quark.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.gb2022.commons.reflect.AutoRegister;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.util.CachedServerIcon;
import org.tbstcraft.quark.api.QueryPingEvent;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.http.HttpHandlerContext;
import org.tbstcraft.quark.internal.http.HttpRequest;

@QuarkModule(version = "1.0.0")
@AutoRegister(ServiceType.HTTP_SERVER)
public final class ServerQueries extends PackageModule {

    @HttpRequest("/query/players")
    public void queryPlayers(HttpHandlerContext context) {
        JsonObject object = context.createJsonReturn();

        JsonArray array = new JsonArray();
        for (Player p : Bukkit.getOnlinePlayers()) {
            JsonObject player = new JsonObject();
            player.addProperty("name", p.getName());
            player.addProperty("uuid", p.getUniqueId().toString());
            player.addProperty("display_name", PlainTextComponentSerializer.plainText().serialize(p.displayName()));

            PlayerProfile profile = p.getPlayerProfile();

            if (profile.getTextures().getSkin() != null) {
                player.addProperty("skin", profile.getTextures().getSkin().toString());
            }
            if (profile.getTextures().getCape() != null) {
                player.addProperty("cape", profile.getTextures().getCape().toString());
            }
            array.add(player);
        }

        object.add("players", array);
    }

    @HttpRequest("/query/ping")
    public void queryMotd(HttpHandlerContext context) {
        JsonObject object = context.createJsonReturn();
        QueryPingEvent event = BukkitUtil.callEvent(new QueryPingEvent());

        object.addProperty("motd", event.getMotd());
        object.addProperty("max-players", event.getMaxPlayers());
        object.addProperty("players", event.getOnlinePlayers());


        CachedServerIcon icon = event.getServerIcon();

        if (icon == null) {
            return;
        }
        object.addProperty("icon", icon.getData());
    }


}
