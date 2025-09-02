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
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.data.record.RecordEntry;

import java.util.Date;
import java.util.List;

@AutoRegister(ServiceType.EVENT_LISTEN)
@SLModule(version = "1.2.2", recordFormat = {})
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

        if (ConfigAccessor.getBool(this.getConfig(), "op-ignore") && p.isOp()) {
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
                MessageAccessor.send(this.language, p, "illegal-item", m.getKey().toString());
            } else {
                MessageAccessor.send(this.language, p, "warning-item", m.getKey().toString());
            }
        }

        if (ConfigAccessor.getBool(this.getConfig(), "record")) {
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

        if (ConfigAccessor.getBool(this.getConfig(), "broadcast")) {
            if (itemIllegal) {
               MessageAccessor.broadcast(this.language, true, false, "illegal-item-broadcast", p.getName(), m.getKey().toString());
            } else {
               MessageAccessor.broadcast(this.language, true, false, "warning-item-broadcast", p.getName(), m.getKey().toString());
            }
        }
    }

    private boolean isItemIllegal(Material material) {
        List<String> list = ConfigAccessor.configList(getConfig(), "illegal-list", String.class);
        return list.contains(material.getKey().getKey());
    }

    private boolean isItemWarning(Material material) {
        List<String> list = ConfigAccessor.configList(getConfig(), "warning-list", String.class);
        return list.contains(material.getKey().getKey());
    }
}