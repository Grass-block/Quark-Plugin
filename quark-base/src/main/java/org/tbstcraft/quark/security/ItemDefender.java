package org.tbstcraft.quark.security;

import me.gb2022.apm.local.PluginMessenger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.EventListener;

import java.util.Date;
import java.util.List;

@EventListener
@QuarkModule(version = "1.2.2", recordFormat = {"Time", "Level", "Player", "World", "X", "Y", "Z", "Type", "Action"})
public final class ItemDefender extends PackageModule {

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        this.checkEvent(event.getPlayer(), event.getPlayer().getInventory().getItem(event.getNewSlot()), true, "Held");
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        this.checkEvent(event.getPlayer(), event.getItem(), true, "Consume");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        this.checkEvent(event.getPlayer(), event.getItem(), false, "Interact");
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent event) {
        this.checkEvent(event.getPlayer(), event.getMainHandItem(), true, "Swap");
        this.checkEvent(event.getPlayer(), event.getOffHandItem(), true, "Swap");
    }

    private void checkEvent(Player p, ItemStack stack, boolean say, String action) {
        if (stack == null) {
            return;
        }

        if (this.getConfig().getBoolean("op-ignore") && p.isOp()) {
            return;
        }

        Material m = stack.getType();
        boolean b1 = this.isItemIllegal(m);
        boolean b2 = this.isItemWarning(m);

        if (!(b1 || b2)) {
            return;
        }

        if (b1) {
            p.getInventory().remove(m);
        }

        if (b1 && b2) {
            b2 = false;
        }

        if (say) {
            if (b1) {
                this.getLanguage().sendMessageTo(p, "illegal-item", m.getKey().toString());
            } else {
                this.getLanguage().sendMessageTo(p, "warning-item", m.getKey().toString());
            }
        }

        if (this.getConfig().getBoolean("record")) {
            this.getRecord().addLine(
                    SharedObjects.DATE_FORMAT.format(new Date()),
                    b2 ? "Warning" : "Illegal",
                    p.getName(),
                    p.getLocation().getWorld().getName(),
                    p.getLocation().getBlockX(),
                    p.getLocation().getBlockY(),
                    p.getLocation().getBlockZ(),
                    m.getKey().toString(),
                    action
            );
        }

        PluginMessenger.broadcastMapped("item:access", (map) -> map
                        .put("player", p.getName())
                        .put("type", b1 ? "illegal" : "warning")
                        .put("item", m.getKey().getKey()));

        if (this.getConfig().getBoolean("broadcast")) {
            if (b1) {
                this.getLanguage().broadcastMessage(true, "illegal-item-broadcast", p.getName(), m.getKey().toString());
            } else {
                this.getLanguage().broadcastMessage(true, "warning-item-broadcast", p.getName(), m.getKey().toString());
            }
        }
    }

    private boolean isItemIllegal(Material material) {
        List<String> list = this.getConfig().getStringList("illegal-list");
        return list.contains(material.getKey().getKey());
    }

    private boolean isItemWarning(Material material) {
        List<String> list = this.getConfig().getStringList("warning-list");
        return list.contains(material.getKey().getKey());
    }
}