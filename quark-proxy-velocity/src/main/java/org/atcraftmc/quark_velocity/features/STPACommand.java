package org.atcraftmc.quark_velocity.features;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.gb2022.commons.container.RequestStorage;
import org.atcraftmc.quark_velocity.Config;
import org.atcraftmc.quark_velocity.ProxyModule;
import org.atcraftmc.quark_velocity.util.VelocityCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class STPACommand extends ProxyModule {

    @Override
    public void enable() {
        getCommandManager().registerCommand(new To());
        getCommandManager().registerCommand(new Here());
    }

    public abstract class AbstractedTPACommand implements SimpleCommand {
        private final RequestStorage storage = new RequestStorage();

        @Override
        public List<String> suggest(Invocation invocation) {
            if (invocation.arguments().length != 0) {
                return List.of();
            }

            if (!(invocation.source() instanceof Player p)) {
                return List.of();
            }

            var current = p.getUsername();
            var list = new ArrayList<String>();

            if (invocation.arguments().length == 0) {
                return List.of("request", "accept", "deny");
            }

            if (invocation.arguments().length == 1 && Objects.equals(invocation.arguments()[0], "request")) {
                for (Player player : getProxy().getAllPlayers()) {
                    var name = player.getCurrentServer().orElseThrow().getServer().getServerInfo().getName();

                    if (Objects.equals(name, current)) {
                        continue;
                    }

                    list.add(player.getUsername());
                }
                return list;
            }

            if (invocation.arguments().length == 1) {

                if (Objects.equals(invocation.arguments()[0], "accept")) {
                    for (Player player : getProxy().getAllPlayers()) {
                        var name = player.getCurrentServer().orElseThrow().getServer().getServerInfo().getName();

                        if (Objects.equals(name, current)) {
                            continue;
                        }

                        list.add(player.getUsername());
                    }
                    return list;
                }

                return new ArrayList<>(this.storage.getRequestList(current));
            }

            return List.of();
        }

        @Override
        public void execute(Invocation invocation) {
            var lang = Config.language("stpa-command");

            if (!(invocation.source() instanceof Player p)) {
                return;
            }

            var target = getProxy().getPlayer(invocation.arguments()[1]);

            if (target.isEmpty()) {
                lang.sendMessage(p, "player-not-found", invocation.arguments()[1]);
                return;
            }

            String senderName = p.getUsername();
            String targetName = target.orElseThrow().getUsername();

            switch (invocation.arguments()[0]) {
                case "accept" -> {
                    if (!getStorage().containsRequest(senderName, targetName)) {
                        lang.sendMessage(p, this.messageNamespace() + "-no-request", targetName);
                        return;
                    }
                    this.onAccepted(p, target.orElseThrow());
                    lang.sendMessage(p, this.messageNamespace() + "-accept-sender", targetName);
                    lang.sendMessage(target.orElseThrow(), this.messageNamespace() + "-accept-target", senderName);
                    getStorage().removeRequest(senderName, targetName);
                }
                case "deny" -> {
                    if (!getStorage().containsRequest(senderName, targetName)) {
                        lang.sendMessage(p, this.messageNamespace() + "-no-request", targetName);
                        return;
                    }
                    lang.sendMessage(p, this.messageNamespace() + "-deny-sender", targetName);
                    lang.sendMessage(target.orElseThrow(), this.messageNamespace() + "-deny-target", senderName);
                    getStorage().removeRequest(senderName, targetName);
                }
                case "request" -> {
                    lang.sendMessage(p, this.messageNamespace() + "-request-sender", targetName);
                    lang.sendMessage(target.orElseThrow(), this.messageNamespace() + "-request-target", senderName, senderName, senderName);
                    getStorage().addRequest(targetName, senderName);
                }
            }
        }

        protected abstract String messageNamespace();

        public RequestStorage getStorage() {
            return this.storage;
        }

        public abstract void onAccepted(Player handler, Player target);
    }

    @VelocityCommand(name = "stpa")
    public class To extends AbstractedTPACommand {

        @Override
        protected String messageNamespace() {
            return "tpa";
        }

        @Override
        public void onAccepted(Player handler, Player target) {
            target.createConnectionRequest(handler.getCurrentServer().orElseThrow().getServer()).connect();
        }
    }

    @VelocityCommand(name = "stpa-here", aliases = "stpa")
    public class Here extends AbstractedTPACommand {

        @Override
        protected String messageNamespace() {
            return "tpahere";
        }

        @Override
        public void onAccepted(Player handler, Player target) {
            handler.createConnectionRequest(target.getCurrentServer().orElseThrow().getServer()).connect();

        }
    }

}
