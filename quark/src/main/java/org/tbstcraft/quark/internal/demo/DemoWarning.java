package org.tbstcraft.quark.internal.demo;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.EventListener;

@EventListener
@QuarkModule(id="demo_warning")
public class DemoWarning extends PackageModule {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if(event.getPlayer().isOp()){

        }else{

        }
    }
}
