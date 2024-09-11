package org.atcraftmc.quark.chat;

import me.gb2022.commons.http.HttpMethod;
import me.gb2022.commons.http.HttpRequest;
import me.gb2022.commons.reflect.AutoRegister;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.foundation.text.ComponentSerializer;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
import org.tbstcraft.quark.internal.task.TaskService;

import java.net.ConnectException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@QuarkModule
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class QQChatSync extends PackageModule {
    public static void send(String address, String token, String group, String message, Logger handle) {

        String parsed = StringEscapeUtils.escapeHtml4(URLEncoder.encode(message, StandardCharsets.UTF_8));

        try {
            HttpRequest.http(HttpMethod.POST, address)
                    .path("/send_group_msg")
                    .browserBehavior(true)
                    .param("access_token", token)
                    .param("group_id", group)
                    .param("message", parsed)
                    .header("Content-Type", null)
                    .header("Encoding", "UTF-8")
                    .build()
                    .request();
        } catch (RuntimeException e) {
            try {
                throw e.getCause();
            } catch (ConnectException ex) {
                handle.severe("failed to connect to LLOneBot server: " + address);
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        TaskService.async().run(() -> {
            var fmt = getConfig().getString("message");

            var msg = fmt.formatted(event.getPlayer().getName(), event.getMessage());
            var token = getConfig().getString("token");
            var address = getConfig().getString("address");
            var message = ComponentSerializer.plain(TextBuilder.buildComponent(PlaceHolderService.formatPlayer(event.getPlayer(), msg)));

            for (String target : getConfig().getList("targets")) {
                send(address, token, target, message, this.getLogger());
            }
        });
    }
}