package org.tbstcraft.quark.tweaks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import me.gb2022.commons.reflect.AutoRegister;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.util.platform.PlayerUtil;

import java.util.HashMap;
import java.util.Map;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.0.0")
@QuarkCommand(name = "freecam", playerOnly = true)
public final class FreeCam extends CommandModule {
    private final Map<String, GameMode> gameModes = new HashMap<>();
    private final Map<String, Location> locations = new HashMap<>();

    @Override
    public void disable() {
        super.disable();
        for (Player p : Bukkit.getOnlinePlayers()) {
            reset(p);
        }
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
