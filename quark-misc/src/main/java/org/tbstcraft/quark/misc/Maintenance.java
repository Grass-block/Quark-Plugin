package org.tbstcraft.quark.misc;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;

import java.util.List;

@QuarkModule
public class Maintenance extends PluginModule {
    private final CommandHandler command = new CommandHandler(this);
    boolean isEnabled = false;

    @Override
    public void onEnable() {
        this.registerListener();
        this.registerCommand(this.command);
    }

    @Override
    public void onDisable() {
        this.unregisterListener();
        this.unregisterCommand(this.command);
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!this.isEnabled) {
            return;
        }
        Player p = Bukkit.getOfflinePlayer(event.getName()).getPlayer();

        String message = this.getLanguage().getMessage("zh_cn", "kick_message");

        if (p != null) {
            message = this.getLanguage().getMessage(p.getPlayer().getLocale(), "kick_message");
            if (p.isOp()) {
                return;
            }
        }
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
    }

    public void kickAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                continue;
            }
            player.kickPlayer(this.getLanguage().getMessage(player.getLocale(), "kick_message"));
        }
    }

    @QuarkCommand(name = "maintenance", op = true)
    public static final class CommandHandler extends ModuleCommand<Maintenance> {
        public CommandHandler(Maintenance module) {
            super(module);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.checkException(args.length == 1);
            switch (args[0]) {
                case "true" -> {
                    this.getLanguage().sendMessageTo(sender, "start");
                    this.getModule().isEnabled = true;
                    this.getModule().kickAll();
                }
                case "false" -> {
                    this.getLanguage().sendMessageTo(sender, "end");
                    this.getModule().isEnabled = false;
                }
                default -> this.sendExceptionMessage(sender);
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
            tabList.add("true");
            tabList.add("false");
        }
    }
}
