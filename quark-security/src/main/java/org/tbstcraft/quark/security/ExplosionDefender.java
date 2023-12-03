package org.tbstcraft.quark.security;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;

import java.util.Date;
import java.util.Objects;

@QuarkModule
public final class ExplosionDefender extends PluginModule {
    @Override
    public void onEnable() {
        this.registerListener();
    }

    @Override
    public void onDisable() {
        this.unregisterListener();
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Block b = event.getBlock();
        event.setCancelled(true);
        this.handle(b.getLocation(), b.getType().getKey().getKey());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity e = event.getEntity();
        event.setCancelled(true);
        this.handle(e.getLocation(), e.getType().getKey().getKey());
    }

    public void handle(Location loc, String explodedId) {
        if (this.getConfig().getBoolean("override_explosion")) {
            Objects.requireNonNull(loc.getWorld()).createExplosion(loc, 4f, false, false);
        }
        if (this.getConfig().getBoolean("broadcast")) {
            this.getLanguage().broadcastMessage(true, "block_exploded",
                    Objects.requireNonNull(loc.getWorld()).getName(),
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ(),
                    explodedId
            );
        }
        if (this.getConfig().getBoolean("record")) {
            this.getRecordEntry().record("[%s]world:%s pos:%s,%s,%s type:%s",
                    SharedObjects.DATE_FORMAT.format(new Date()),
                    Objects.requireNonNull(loc.getWorld()).getName(),
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ(),
                    explodedId
            );
        }
    }
}