package org.tbstcraft.quark.utilities;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.tbstcraft.quark.framework.command.CommandRegistry;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.event.messenging.Messenger;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.service.PermissionService;

import java.util.List;

@SuppressWarnings("deprecation")
@QuarkModule(version = "1.0.0")
@EventListener
@CommandRegistry(Maintenance.MaintenanceCommand.class)
public class Maintenance extends PackageModule {
    boolean isEnabled = false;

    @Override
    public void enable() {
        PermissionService.createPermission("-quark.maintenance.bypass");
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!this.isEnabled) {
            return;
        }
        Player p = Bukkit.getOfflinePlayer(event.getName()).getPlayer();
        String locale = "zh-cn";

        if (p != null) {
            locale = p.getLocale();
            if (p.hasPermission("quark.maintenance.bypass")) {
                return;
            }
        }
        String message = this.getLanguage().getMessage(locale, "kick-message");
        String name = event.getPlayerProfile().getName();

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messenger.queryKickMessage(name, message, locale));
    }

    public void kickAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("quark.maintenance.bypass")) {
                continue;
            }

            String locale = player.getLocale();
            String msg = this.getLanguage().getMessage(locale, "kick-message");
            player.kickPlayer(msg);
        }
    }

    @QuarkCommand(name = "maintenance", permission = "-quark.maintenance.command")
    public static final class MaintenanceCommand extends ModuleCommand<Maintenance> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.checkException(args.length == 1);
            switch (args[0]) {
                case "enable" -> {
                    this.getLanguage().sendMessageTo(sender, "start");
                    this.getModule().isEnabled = true;
                    this.getModule().kickAll();
                }
                case "disable" -> {
                    this.getLanguage().sendMessageTo(sender, "end");
                    this.getModule().isEnabled = false;
                }
                default -> this.sendExceptionMessage(sender);
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            tabList.add("enable");
            tabList.add("disable");
        }
    }
}
