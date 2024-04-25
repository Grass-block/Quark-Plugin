package org.tbstcraft.quark.tweaks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

@EventListener
@QuarkModule(id = "movable_containers",version = "0.3",beta = true)
public final class MovableContainers extends PackageModule {
    @EventHandler
    public void onPistonExtract(BlockPistonExtendEvent event){
        System.out.println("re");
        event.getBlocks();
    }
}
