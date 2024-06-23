package org.tbstcraft.quark.proxysupport;

import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteMessageEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.RemoteMessageService;

import java.util.Objects;

//the smallest module ever...
@QuarkModule(version = "1.0.0")
@AutoRegister({ServiceType.EVENT_LISTEN, ServiceType.REMOTE_MESSAGE})
public final class ChatSync extends PackageModule {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        RemoteMessageService.broadcast("quark:chat/sync", (b) -> {
            BufferUtil.writeString(b, event.getPlayer().getDisplayName());
            BufferUtil.writeString(b, event.getMessage());
        });
    }

    @RemoteEventHandler("quark:chat/sync")
    public void onChatAsync(RemoteMessageEvent event) {
        Bukkit.getServer().broadcastMessage(Objects.requireNonNull(this.getConfig().getString("format")).formatted(
                BufferUtil.readString(event.getData()),
                BufferUtil.readString(event.getData())
        ));
    }
}
