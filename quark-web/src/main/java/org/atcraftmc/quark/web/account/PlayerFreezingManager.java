package org.atcraftmc.quark.web.account;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark.chat.ChatForwardingService;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.migration.MessageAccessor;

import java.util.HashSet;
import java.util.Set;

@AutoRegister(ServiceType.EVENT_LISTEN)
public final class PlayerFreezingManager extends ModuleComponent<AccountActivation> {
    private final Set<String> whiteListedPlayers = new HashSet<>();

    public PlayerFreezingManager(AccountActivation module) {
        super(module);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("/account")) {
            return;
        }
        this.checkPlayerAction(event, event.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        this.checkPlayerAction(event, event.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        this.checkPlayerAction(event, event.getPlayer(), false);
    }

    @EventHandler
    public void onPlayerChat(ChatForwardingService.ChatForwardingEvent event) {
        if(event.isSenderConsole()){
            return;
        }
        this.checkPlayerAction(event, event.getSenderAsPlayer(), false);
    }

    @EventHandler
    public void onPlayerGamemodeChange(PlayerGameModeChangeEvent event) {
        this.checkPlayerAction(event, event.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        this.checkPlayerAction(event, event.getPlayer(), false);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        this.checkPlayerAction(event, event.getPlayer(), false);
    }

    public void checkPlayerAction(Cancellable event, Player p, boolean sendMessage) {
        if (this.whiteListedPlayers.contains(p.getName())) {
            return;
        }
        event.setCancelled(true);
        if (!sendMessage) {
            return;
        }
        MessageAccessor.send(this.getLanguage(), p, "interaction-block");
    }

    public void freezePlayer(String name) {
        this.whiteListedPlayers.remove(name);
    }

    public void unfreezePlayer(String name) {
        this.whiteListedPlayers.add(name);
    }

    public Set<String> getWhiteList() {
        return this.whiteListedPlayers;
    }
}
