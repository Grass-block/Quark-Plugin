package org.tbstcraft.quark.security;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.service.WorldEditLocalSessionTracker;
import org.tbstcraft.quark.util.Region;

import java.util.Objects;

@QuarkModule
public class WorldEditSectionSizeDefender extends PluginModule {
    @Override
    public void onEnable() {
        this.registerListener();
    }

    @Override
    public void onDisable() {
        this.unregisterListener();
    }

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
        Region r = WorldEditLocalSessionTracker.getRegion(event.getPlayer());
        if (event.getMessage().startsWith("//")) {
            if(event.getMessage().startsWith("//stack")){
                int opCount = Integer.parseInt(event.getMessage().split(" ")[1]);
                if (opCount * r.asAABB().getMaxWidth() > this.getConfig().getInt("total_size") ||
                        opCount > this.getConfig().getInt("operation_size")) {
                    event.setCancelled(true);
                    this.getLanguage().sendMessageTo(event.getPlayer(), "interact_blocked_we");
                    if (!this.getConfig().getBoolean("record")) {
                        return;
                    }
                    this.getRecordEntry().record("player:%s world:%s session:%s",
                            event.getPlayer().getName(),
                            Objects.requireNonNull(event.getPlayer().getEyeLocation().getWorld()).getName(),
                            r.toString()
                    );
                }
            }else {
                Player player = event.getPlayer();
                if (r.asAABB().getMaxWidth() > this.getConfig().getInt("selection_size")) {
                    event.setCancelled(true);
                    this.getLanguage().sendMessageTo(player, "interact_blocked_we");
                }
            }
            if (!this.getConfig().getBoolean("record")) {
                return;
            }
            this.getRecordEntry().record("player:%s world:%s session:%s", event.getPlayer().getName(), Objects.requireNonNull(event.getPlayer().getEyeLocation().getWorld()).getName(), r.toString());
        }
    }
}
