package org.atcraftmc.quark.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.atcraftmc.quark.web.http.HttpHandlerContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.CachedServerIcon;
import org.atcraftmc.starlight.api.event.QueryPingEvent;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;

@SLModule(version = "1.0.0")
public final class ServerQueries extends PackageModule {

    @Override
    public void checkCompatibility() {
        Compatibility.requireClass(() -> Class.forName("com.destroystokyo.paper.profile.PlayerProfile"));
        HttpService.registerHandler(this);
    }

    public void queryPlayers(HttpHandlerContext context) {
        JsonObject object = context.createJsonReturn();

        JsonArray array = new JsonArray();
        for (Player p : Bukkit.getOnlinePlayers()) {
            JsonObject player = new JsonObject();
            player.addProperty("name", p.getName());
            player.addProperty("uuid", p.getUniqueId().toString());
            player.addProperty("display_name", PlainTextComponentSerializer.plainText().serialize(p.displayName()));

            var profile = p.getPlayerProfile();

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

    public void queryMotd(HttpHandlerContext context) {
        JsonObject object = context.createJsonReturn();
        BukkitUtil.callEvent(new QueryPingEvent(), (event) -> {
            object.addProperty("motd", event.getMotd());
            object.addProperty("max-players", event.getMaxPlayers());
            object.addProperty("players", event.getOnlinePlayers());


            CachedServerIcon icon = event.getServerIcon();

            if (icon == null) {
                return;
            }
            object.addProperty("icon", icon.getData());
        });
    }
}
