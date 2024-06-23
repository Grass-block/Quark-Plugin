package org.tbstcraft.quark.security.account;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;
import org.tbstcraft.quark.framework.module.component.ModuleComponent;

import java.util.HashSet;
import java.util.Set;

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
        this.getLanguage().sendMessage(p, "interaction-block");
    }

    public void freezePlayer(String name) {
        this.whiteListedPlayers.remove(name);
    }

    public void unfreezePlayer(String name) {
        this.whiteListedPlayers.add(name);
    }
}
