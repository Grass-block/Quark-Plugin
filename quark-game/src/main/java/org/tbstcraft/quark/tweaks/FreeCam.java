package org.tbstcraft.quark.tweaks;

import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.data.PlaceHolderStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import me.gb2022.commons.reflect.AutoRegister;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.0.0")
@QuarkCommand(name = "freecam", playerOnly = true)
public final class FreeCam extends CommandModule {
    private final Map<String, GameMode> gameModes = new HashMap<>();
    private final Map<String, Location> locations = new HashMap<>();

    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PlaceHolderStorage.get(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, HashSet.class, (s) -> s.add(this.tip));
        super.enable();
    }

    @Override
    public void disable(){
        PlaceHolderStorage.get(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, HashSet.class, (s) -> s.remove(this.tip));
        for (Player p : Bukkit.getOnlinePlayers()) {
            reset(p);
        }
        super.disable();
    }

    public void reset(Player player) {
        String id = player.getName();
        if (!this.gameModes.containsKey(id)) {
            return;
        }
        player.setGameMode(this.gameModes.get(id));
        PlayerUtil.teleport(player, this.locations.get(id));
        this.gameModes.remove(id);
        this.locations.remove(id);
        this.getLanguage().sendMessage(player, "reset");
    }

    public void start(Player player) {
        String id = player.getName();
        if (this.gameModes.containsKey(id)) {
            return;
        }
        this.locations.put(id, player.getLocation());
        this.gameModes.put(id, player.getGameMode());
        player.setGameMode(GameMode.SPECTATOR);
        this.getLanguage().sendMessage(player, "start");
    }

    public void toggle(Player p) {
        if (gameModes.containsKey(p.getName())) {
            this.reset(p);
        } else {
            this.start(p);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.reset(event.getPlayer());
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        this.toggle((Player) sender);
    }
}
