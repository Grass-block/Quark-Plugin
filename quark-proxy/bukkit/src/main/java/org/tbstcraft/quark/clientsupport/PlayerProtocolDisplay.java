package org.tbstcraft.quark.clientsupport;

import me.gb2022.apm.client.event.ClientProtocolInitEvent;
import me.gb2022.apm.client.event.driver.ClientEventHandler;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import me.gb2022.commons.reflect.AutoRegister;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;

@QuarkModule(version = "0.7")
@AutoRegister(ServiceType.CLIENT_MESSAGE)
public final class PlayerProtocolDisplay extends PackageModule {

    @ClientEventHandler
    public void onPlayerProtocolInitialized(ClientProtocolInitEvent event){
        this.getLanguage().sendMessage(PlayerUtil.strictFindPlayer(event.getPlayer()),"protocol-init");
    }
}
