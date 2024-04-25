package org.tbstcraft.quark.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.util.api.APIProfile;

@QuarkModule(version = "1.0.0", compatBlackList = {APIProfile.SPIGOT, APIProfile.ARCLIGHT})
public class ChatReport extends PackageModule {


    @EventHandler
    public void onChat(AsyncChatEvent event){
        event.message(event.message().append(getLanguage().getMessageComponent("zh_cn","message")));
    }


}
