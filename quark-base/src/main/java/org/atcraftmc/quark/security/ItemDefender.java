package org.atcraftmc.quark.security;

import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.framework.record.RecordEntry;

import java.util.Date;
import java.util.List;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.2.2", recordFormat = {})
public final class ItemDefender extends PackageModule {

    @Inject
    private LanguageEntry language;

    @Inject("item-defender;Time,Level,Player,World,X,Y,Z,Type,Action")
    private RecordEntry record;

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
        boolean itemIllegal = this.isItemIllegal(m);
        boolean b2 = this.isItemWarning(m);

        if (!(itemIllegal || b2)) {
            return;
        }

        if (itemIllegal) {
            p.getInventory().remove(m);
        }

        if (itemIllegal && b2) {
            b2 = false;
        }

        if (say) {
            if (itemIllegal) {
                this.language.sendMessage(p, "illegal-item", m.getKey().toString());
            } else {
                this.language.sendMessage(p, "warning-item", m.getKey().toString());
            }
        }

        if (this.getConfig().getBoolean("record")) {
            this.record.addLine(
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
                        .put("type", itemIllegal ? "illegal" : "warning")
                        .put("item", m.getKey().getKey()));

        if (this.getConfig().getBoolean("broadcast")) {
            if (itemIllegal) {
                this.language.broadcastMessage(true,false, "illegal-item-broadcast", p.getName(), m.getKey().toString());
            } else {
                this.language.broadcastMessage(true,false, "warning-item-broadcast", p.getName(), m.getKey().toString());
            }
        }
    }

    private boolean isItemIllegal(Material material) {
        List<String> list = this.getConfig().getList("illegal-list");
        return list.contains(material.getKey().getKey());
    }

    private boolean isItemWarning(Material material) {
        List<String> list = this.getConfig().getList("warning-list");
        return list.contains(material.getKey().getKey());
    }
}