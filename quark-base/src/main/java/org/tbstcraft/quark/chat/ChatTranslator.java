package org.tbstcraft.quark.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.module.services.EventListener;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;

@EventListener
@QuarkModule(id = "chat-translator", version = "_dev", beta = true)
public class ChatTranslator extends PackageModule {

    @EventHandler
    public void onChatting(AsyncPlayerChatEvent event) {
        //todo
    }
}
