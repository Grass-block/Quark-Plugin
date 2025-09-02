package org.atcraftmc.quark_velocity.features;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.language.LanguageContainer;
import org.atcraftmc.quark_velocity.ProxyModule;
import org.atcraftmc.quark_velocity.QuarkVelocity;
import org.atcraftmc.quark_velocity.Registers;

import java.util.HashSet;
import java.util.Set;

@AutoRegister(Registers.VELOCITY_EVENT)
public final class PlayerActionRestriction extends ProxyModule {
    private static final ChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from("quark_pl:player");
    private final Set<String> players = new HashSet<>();

    @Override
    public void enable() {
        getProxy().getChannelRegistrar().register(CHANNEL);
    }

    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event) {
        if (!CHANNEL.equals(event.getIdentifier())) {
            return;
        }

        if (!(event.getSource() instanceof ServerConnection connection)) {
            return;
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        var player = connection.getPlayer();
        var sig = event.getData()[0];

        if (sig == 0x01) {
            this.players.add(player.getUsername());
            getLogger().info("locked player '{}' by the request of '{}'", player.getUsername(), connection.getServerInfo().getName());
        }
        if (sig == 0x02) {
            this.players.remove(player.getUsername());
            getLogger().info("unlocked player '{}' by the request of '{}'", player.getUsername(), connection.getServerInfo().getName());
        }
    }


    @Subscribe
    public void onPlayerCommand(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player p)) {
            return;
        }

        if (!this.players.contains(p.getUsername())) {
            return;
        }

        if (event.getCommand().startsWith("account") || event.getCommand().startsWith("/account")) {
            return;
        }

        QuarkVelocity.lang().entry("--global", "player-action-restriction").sendMessage(p, "restricted");

        event.setResult(CommandExecuteEvent.CommandResult.denied());
    }
}
