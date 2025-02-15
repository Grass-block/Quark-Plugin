package org.atcraftmc.quark_velocity.features;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.atcraftmc.quark_velocity.ProxyModule;
import org.atcraftmc.quark_velocity.util.VelocityCommand;

@VelocityCommand(name = "hub", aliases = {"lobby", "quit"})
public final class HUBCommand extends ProxyModule implements SimpleCommand {

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
        var source = invocation.source();

        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("[Quark-Velocity] You must be a player to use this command!", NamedTextColor.RED));
            return;
        }

        this.getProxy().getServer("lobby").ifPresentOrElse(
                (lobby) -> player.createConnectionRequest(lobby).connect(),
                () -> player.sendMessage(Component.text("[Quark-Velocity] No lobby server found!", NamedTextColor.RED))
                                                           );
    }
}
