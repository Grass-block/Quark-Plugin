package org.atcraftmc.quark.clientsupport;

import me.gb2022.apm.client.event.ClientProtocolInitEvent;
import me.gb2022.apm.client.event.driver.ClientEventHandler;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Bukkit;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

@QuarkModule(version = "0.7")
@AutoRegister(ServiceType.CLIENT_MESSAGE)
public final class PlayerProtocolDisplay extends PackageModule {

    @ClientEventHandler
    public void onPlayerProtocolInitialized(ClientProtocolInitEvent event){
        String name = event.getPlayer();
        this.getLanguage().sendMessage(Bukkit.getPlayerExact(name),"protocol-init");
    }
}
