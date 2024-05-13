package org.tbstcraft.quark.security;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.service.WESessionTrackService;
import org.tbstcraft.quark.util.region.Region;
import org.tbstcraft.quark.util.region.SimpleRegion;

import java.util.Objects;

@QuarkModule(version = "1.2.5")
@EventListener
public class WorldEditSectionSizeDefender extends PackageModule {
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            return;
        }
        if (Objects.requireNonNull(event.getPlayer()).isOp()) {
            return;
        }
        if (event.getMessage().startsWith("//pos1")) {
            return;
        }
        if (event.getMessage().startsWith("//pos2")) {
            return;
        }
        Region r = WESessionTrackService.getRegion(event.getPlayer());
        if (event.getMessage().startsWith("//")) {
            if(event.getMessage().startsWith("//stack")){
                int opCount = Integer.parseInt(event.getMessage().split(" ")[1]);
                if (opCount * r.asAABB().getMaxWidth() > this.getConfig().getInt("total-size") ||
                        opCount > this.getConfig().getInt("operation-size")) {
                    event.setCancelled(true);
                    this.getLanguage().sendMessageTo(event.getPlayer(), "interact-blocked-we");
                    if (!this.getConfig().getBoolean("record")) {
                        return;
                    }
                    //todo record
                }
            }else {
                Player player = event.getPlayer();
                if (r.asAABB().getMaxWidth() > this.getConfig().getInt("selection_size")) {
                    event.setCancelled(true);
                    this.getLanguage().sendMessageTo(player, "interact-blocked-we");
                }
            }
            if (!this.getConfig().getBoolean("record")) {
                return;
            }
            //todo record
            //this.getRecord().record("player:%s world:%s session:%s", event.getPlayer().getName(), Objects.requireNonNull(event.getPlayer().getEyeLocation().getWorld()).getName(), r.toString());
        }
    }
}
