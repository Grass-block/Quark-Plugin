package org.atcraftmc.quark.velocity.features;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import org.atcraftmc.quark.velocity.ProxyModule;
import org.atcraftmc.quark.velocity.command.VelocityCommand;

@VelocityCommand(name = "hub", aliases = {"lobby","quit"})
public final class HubCommand extends ProxyModule implements SimpleCommand {

    @Override
    public void enable() {
        this.getCommandManager().registerCommand(this);
    }

    @Override
    public void disable() {
        this.getCommandManager().unregisterCommand(this);
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendPlainMessage("[Quark-Velocity] You must be a player to use this command!");
            return;
        }

        this.getServer().getServer("lobby").ifPresentOrElse(
                (lobby) -> player.createConnectionRequest(lobby).connect(),
                () -> player.sendPlainMessage("[Quark-Velocity] No lobby server found!")
        );
    }
}
