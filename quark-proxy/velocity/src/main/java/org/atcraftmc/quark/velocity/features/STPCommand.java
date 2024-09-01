package org.atcraftmc.quark.velocity.features;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.atcraftmc.quark.velocity.ProxyModule;
import org.atcraftmc.quark.velocity.command.VelocityCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@VelocityCommand(name = "stp", aliases = {"proxy-tp"})
public final class STPCommand extends ProxyModule implements SimpleCommand {

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

        try {
            var name = invocation.arguments()[0];
            var target = getServer().getPlayer(name).orElseThrow();
            var server = target.getCurrentServer().orElseThrow().getServer();

            player.createConnectionRequest(server).connect();
        } catch (ArrayIndexOutOfBoundsException e) {
            player.sendMessage(Component.text("[Quark-Velocity] Please specify a player.", NamedTextColor.RED));
        } catch (Exception e) {
            player.sendMessage(Component.text("[Quark-Velocity] Player Not Found.", NamedTextColor.RED));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length != 0) {
            return List.of();
        }

        if (!(invocation.source() instanceof Player p)) {
            return List.of();
        }

        var current = p.getCurrentServer().orElseThrow().getServer().getServerInfo().getName();
        var list = new ArrayList<String>();

        for (Player player : getServer().getAllPlayers()) {
            var name = player.getCurrentServer().orElseThrow().getServer().getServerInfo().getName();

            if (Objects.equals(name, current)) {
                continue;
            }

            list.add(player.getUsername());
        }
        return list;
    }
}
