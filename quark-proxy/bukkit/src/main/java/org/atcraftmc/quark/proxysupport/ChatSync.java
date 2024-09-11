package org.atcraftmc.quark.proxysupport;

import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteMessageEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.foundation.text.TextSender;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.RemoteMessageService;

import java.util.Objects;

@QuarkModule(version = "1.0.0")
@AutoRegister({ServiceType.EVENT_LISTEN, ServiceType.REMOTE_MESSAGE})
public final class ChatSync extends PackageModule {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        RemoteMessageService.message("proxy", "quark:chat/sync/upstream", (b) -> {
            BufferUtil.writeString(b, event.getPlayer().getDisplayName());
            BufferUtil.writeString(b, event.getMessage());
        });
    }

    @RemoteEventHandler("quark:chat/sync/downstream")
    public void onChatAsync(RemoteMessageEvent event) {

        String server = BufferUtil.readString(event.getData());
        if (server.equals(event.getConnector().getIdentifier())) {
            return;
        }

        TextSender.broadcastLine((l) -> {
            String msg = Objects.requireNonNull(this.getConfig().getString("format")).formatted(
                    BufferUtil.readString(event.getData()),
                    BufferUtil.readString(event.getData()),
                    BufferUtil.readString(event.getData()));
            return TextBuilder.buildComponent(msg);
        }, false, false);
    }
}
