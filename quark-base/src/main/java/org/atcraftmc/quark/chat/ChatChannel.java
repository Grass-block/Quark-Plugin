package org.atcraftmc.quark.chat;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.Registrations;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.atcraftmc.qlib.command.QuarkCommand;
import me.gb2022.modular.module.ApplicationModule;

// TODO: 2024/3/10 Implement it
@ApplicationModule(id = "chat-channel", version = "0.0.1", beta = true)
@QuarkCommand(name = "chat")
@AutoRegister(Registrations.SERVER_EVENT)
public class ChatChannel extends SLPackageModule {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(final AsyncPlayerChatEvent event) {
        event.setCancelled(true);
    }
}
