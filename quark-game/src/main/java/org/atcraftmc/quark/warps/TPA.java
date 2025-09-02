package org.atcraftmc.quark.warps;

import me.gb2022.commons.container.RequestStorage;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.StarlightCommandManager;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.foundation.platform.Players;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;

@SLModule(version = "1.0.2")
public final class TPA extends PackageModule {
    private final TPACommand tpa = new TPACommand();
    private final TPAHereCommand tpaHere = new TPAHereCommand();

    @Override
    public void enable() {
        this.tpa.initContext(this);
        this.tpaHere.initContext(this);

        StarlightCommandManager.getInstance().register(this.tpa);
        StarlightCommandManager.getInstance().register(this.tpaHere);

        BukkitUtil.registerEventListener(this.tpa);
        BukkitUtil.registerEventListener(this.tpaHere);
    }

    @Override
    public void disable() {
        StarlightCommandManager.getInstance().unregister(this.tpa);
        StarlightCommandManager.getInstance().unregister(this.tpaHere);

        BukkitUtil.unregisterEventListener(this.tpa);
        BukkitUtil.unregisterEventListener(this.tpaHere);
    }

    public static abstract class AbstractedTPACommand extends ModuleCommand<TPA> {
        private final RequestStorage storage = new RequestStorage();

        @Override
        public void suggest(CommandSuggestion suggestion) {
            String sender = suggestion.getSender().getName();

            suggestion.suggest(0, "request", "accept", "deny");

            suggestion.matchArgument(0, "request", (s) -> s.suggestOnlinePlayers(1));
            suggestion.matchArgument(0, "accept", (s) -> s.suggest(1, this.storage.getRequestList(sender)));
            suggestion.matchArgument(0, "deny", (s) -> s.suggest(1, this.storage.getRequestList(sender)));
        }

        @Override
        public void execute(CommandExecution context) {
            Player sender = context.requireSenderAsPlayer();
            Player target = context.requirePlayer(1);

            String senderName = sender.getName();
            String targetName = target.getName();

            switch (context.requireEnum(0, "request", "accept", "deny")) {
                case "accept" -> {
                    if (!getStorage().containsRequest(senderName, targetName)) {
                        MessageAccessor.send(this.getLanguage(), sender, this.getName() + "-no-request", targetName);
                        return;
                    }
                    this.onAccepted(sender, target);
                    MessageAccessor.send(this.getLanguage(), sender, this.getName() + "-accept-sender", targetName);
                    MessageAccessor.send(this.getLanguage(), target, this.getName() + "-accept-target", senderName);
                    getStorage().removeRequest(senderName, targetName);
                }
                case "deny" -> {
                    if (!getStorage().containsRequest(senderName, targetName)) {
                        MessageAccessor.send(this.getLanguage(), sender, this.getName() + "-no-request", targetName);
                        return;
                    }
                    MessageAccessor.send(this.getLanguage(), sender, this.getName() + "-deny-sender", targetName);
                    MessageAccessor.send(this.getLanguage(), target, this.getName() + "-deny-target", senderName);
                    getStorage().removeRequest(senderName, targetName);
                }
                case "request" -> {
                    MessageAccessor.send(this.getLanguage(), sender, this.getName() + "-request-sender", targetName);
                    MessageAccessor.send(this.getLanguage(), target, this.getName() + "-request-target", senderName, senderName, senderName);
                    getStorage().addRequest(targetName, senderName);
                }
            }
        }

        public RequestStorage getStorage() {
            return this.storage;
        }

        public abstract void onAccepted(Player handler, Player target);

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                this.getStorage().removeRequest(p.getName(), event.getPlayer().getName());
            }
        }
    }

    @QuarkCommand(name = "tpa", permission = "+quark.tpa")
    public static final class TPACommand extends AbstractedTPACommand {

        @Override
        public void onAccepted(Player handler, Player target) {
            Players.teleport(target, handler.getLocation());
        }
    }

    @QuarkCommand(name = "tpahere", permission = "+quark.tpahere")
    public static final class TPAHereCommand extends AbstractedTPACommand {

        @Override
        public void onAccepted(Player handler, Player target) {
            Players.teleport(handler, target.getLocation());
        }
    }
}
