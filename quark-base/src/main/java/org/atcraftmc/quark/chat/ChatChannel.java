package org.atcraftmc.quark.chat;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

// TODO: 2024/3/10 Implement it
@QuarkModule(id = "chat-channel", version = "0.0.1", beta = true)
@QuarkCommand(name = "chat")
@AutoRegister(ServiceType.EVENT_LISTEN)
public class ChatChannel extends PackageModule {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(final AsyncPlayerChatEvent event) {
        event.setCancelled(true);
    }
}
