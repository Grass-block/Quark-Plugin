package org.tbstcraft.quark.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import me.gb2022.commons.reflect.AutoRegister;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(id = "chat-translator", version = "_dev", beta = true)
public class ChatTranslator extends PackageModule {

    @EventHandler
    public void onChatting(AsyncPlayerChatEvent event) {
        //todo
    }
}
