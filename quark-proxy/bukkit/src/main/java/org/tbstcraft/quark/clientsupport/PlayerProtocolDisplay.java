package org.tbstcraft.quark.clientsupport;

import me.gb2022.apm.client.event.ClientProtocolInitEvent;
import me.gb2022.apm.client.event.driver.ClientEventHandler;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ModuleService;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.util.platform.PlayerUtil;

@QuarkModule(version = "0.7")
@ModuleService(ServiceType.CLIENT_MESSAGE)
public final class PlayerProtocolDisplay extends PackageModule {

    @ClientEventHandler
    public void onPlayerProtocolInitialized(ClientProtocolInitEvent event){
        this.getLanguage().sendMessageTo(PlayerUtil.strictFindPlayer(event.getPlayer()),"protocol-init");
    }
}
