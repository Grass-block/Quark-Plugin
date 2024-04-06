package org.tbstcraft.quark.security;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.event.MessageEvent;
import org.tbstcraft.quark.module.services.EventListener;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@EventListener
@QuarkModule(version = "1.2.2")
public final class ItemDefender extends PackageModule {

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        this.checkEvent(event.getPlayer(), event.getPlayer().getInventory().getItem(event.getNewSlot()), true);
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        this.checkEvent(event.getPlayer(), event.getItem(), true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        this.checkEvent(event.getPlayer(), event.getItem(), false);
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent event) {
        this.checkEvent(event.getPlayer(), event.getMainHandItem(), true);
        this.checkEvent(event.getPlayer(), event.getOffHandItem(), true);
    }

    private void checkEvent(Player p, ItemStack stack, boolean say) {
        if (stack == null) {
            return;
        }

        if (this.getConfig().getBoolean("op_ignore") && p.isOp()) {
            return;
        }

        Material m = stack.getType();
        if (this.isItemIllegal(m)) {
            if (say) {
                this.getLanguage().sendMessageTo(p, "illegal_item", m.name());
            }
            p.getInventory().remove(m);
            if (this.getConfig().getBoolean("broadcast")) {
                this.getLanguage().broadcastMessage(true, "illegal_item_broadcast", p.getName(), m.name());
            }
            Bukkit.getPluginManager().callEvent(MessageEvent.builder("defended_item_access")
                    .param("type", "illegal")
                    .param("player", p.getName())
                    .param("item", m.getKey().getKey())
                    .build());
            if (!this.getConfig().getBoolean("record")) {
                return;
            }
            this.getRecord().record("[%s] illegal player=%s item=%s".formatted(new SimpleDateFormat().format(new Date()), p.getName(), m.name()));
        }
        if (this.isItemWarning(m)) {
            if (say) {
                this.getLanguage().sendMessageTo(p, "warning_item", m.name());
            }
            if (this.getConfig().getBoolean("broadcast")) {
                this.getLanguage().broadcastMessage(true, "warning_item_broadcast", p.getName(), m.name());
            }
            Bukkit.getPluginManager().callEvent(MessageEvent.builder("defended_item_access")
                    .param("type", "warning")
                    .param("player", p.getName())
                    .param("item", m.getKey().getKey())
                    .build());
            if (!this.getConfig().getBoolean("record")) {
                return;
            }
            this.getRecord().record("[%s] warning player=%s item=%s".formatted(new SimpleDateFormat().format(new Date()), p.getName(), m.name()));
        }
    }

    private boolean isItemIllegal(Material material) {
        List<String> list = this.getConfig().getStringList("illegal_list");
        return list.contains(material.getKey().getKey());
    }

    private boolean isItemWarning(Material material) {
        List<String> list = this.getConfig().getStringList("warning_list");
        return list.contains(material.getKey().getKey());
    }
}