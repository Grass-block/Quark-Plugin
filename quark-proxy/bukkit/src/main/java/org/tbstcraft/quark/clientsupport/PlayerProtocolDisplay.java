package org.tbstcraft.quark.clientsupport;

import me.gb2022.apm.client.event.ClientProtocolInitEvent;
import me.gb2022.apm.client.event.driver.ClientEventHandler;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ClientMessageListener;
import org.tbstcraft.quark.util.api.PlayerUtil;

@ClientMessageListener
@QuarkModule(version = "0.7")
public final class PlayerProtocolDisplay extends PackageModule {

    @ClientEventHandler
    public void onPlayerProtocolInitialized(ClientProtocolInitEvent event){
        this.getLanguage().sendMessageTo(PlayerUtil.strictFindPlayer(event.getPlayer()),"protocol-init");
    }
}
