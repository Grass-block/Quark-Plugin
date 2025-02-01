package org.atcraftmc.quark.tweaks;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.language.LanguageItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.PlayerIdentificationService;

import java.util.HashMap;
import java.util.Map;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.0.0")
@QuarkCommand(name = "freecam", playerOnly = true)
public final class FreeCam extends CommandModule {
    private final Map<String, GameMode> gameModes = new HashMap<>();
    private final Map<String, Location> locations = new HashMap<>();

    @Inject("tip")
    private LanguageItem tip;

    @Inject("-quark.freecam.bypass")
    private Permission bypassPermission;

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
        super.enable();
    }

    @Override
    public void disable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
        for (Player p : Bukkit.getOnlinePlayers()) {
            reset(p);
        }
        super.disable();
    }

    public void reset(Player player) {
        var id = PlayerIdentificationService.transformPlayer(player);
        if (!this.gameModes.containsKey(id)) {
            return;
        }
        player.setGameMode(this.gameModes.get(id));
        Players.teleport(player, this.locations.get(id));
        this.gameModes.remove(id);
        this.locations.remove(id);
        this.getLanguage().sendMessage(player, "reset");
    }

    public void start(Player player) {
        var id = PlayerIdentificationService.transformPlayer(player);
        if (this.gameModes.containsKey(id)) {
            return;
        }
        this.locations.put(id, player.getLocation());
        this.gameModes.put(id, player.getGameMode());
        player.setGameMode(GameMode.SPECTATOR);
        this.getLanguage().sendMessage(player, "start");
    }

    public void toggle(Player p) {
        if (this.gameModes.containsKey(PlayerIdentificationService.transformPlayer(p))) {
            this.reset(p);
        } else {
            this.start(p);
        }
    }

    private boolean inSession(Player p) {
        return this.gameModes.containsKey(PlayerIdentificationService.transformPlayer(p));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.reset(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setGameMode(Bukkit.getDefaultGameMode());
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        this.toggle((Player) sender);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if(event.getMessage().contains("freecam")){
            return;
        }
        if(!this.getConfig().getBoolean("anti-cheat")){
            return;
        }
        if(event.getPlayer().hasPermission(this.bypassPermission)){
            return;
        }
        if (this.inSession(event.getPlayer())) {
            event.setCancelled(true);
            getLanguage().sendMessage(event.getPlayer(), "anti-cheat");
        }
    }
}
