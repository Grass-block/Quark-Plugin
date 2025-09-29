package org.atcraftmc.quark.proxy;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import me.gb2022.modular.module.ApplicationModule;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@ApplicationModule
@CommandProvider(ServerConnect.ConnectCommand.class)
public class ServerConnect extends SLPackageModule {
    private final Map<String, String> originRecords = new HashMap<>();

    static void connect(Player player, String target) {
        player.sendPluginMessage(Starlight.instance(), "client_transfer:main", target.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void enable() {
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(Starlight.instance(), "client_transfer:main");
    }

    @Override
    public void disable() {
        Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(Starlight.instance(), "client_transfer:main");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.originRecords.containsKey(event.getPlayer().getName())) {

        }
    }

    @QuarkCommand(name = "connect")
    public static final class ConnectCommand extends ModuleCommand<ServerConnect> {
        @Override
        public void execute(CommandExecution context) {
            try {
                connect(context.requireSenderAsPlayer(), context.requireArgumentAt(0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
