package org.atcraftmc.quark.management;

import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.permissions.Permission;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.List;

@SuppressWarnings("deprecation")
@QuarkModule(version = "1.0.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(Maintenance.MaintenanceCommand.class)
public final class Maintenance extends PackageModule {
    boolean isEnabled = false;

    @Inject("-quark.maintenance.bypass")
    private Permission bypass;

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!this.isEnabled) {
            return;
        }
        Player p = Bukkit.getOfflinePlayer(event.getName()).getPlayer();
        String locale = "zh-cn";

        if (p != null) {
            locale = p.getLocale();
            if (p.hasPermission(this.bypass)) {
                return;
            }
        }
        String message = this.getLanguage().getMessage(Language.locale(p), "kick-message");
        String name = event.getPlayerProfile().getName();

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, PluginMessenger.queryKickMessage(name, message, locale));
    }

    public void kickAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(this.bypass)) {
                continue;
            }

            String msg = this.getLanguage().getMessage(Language.locale(player), "kick-message");
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
                    this.getLanguage().sendMessage(sender, "start");
                    this.getModule().isEnabled = true;
                    this.getModule().kickAll();
                }
                case "disable" -> {
                    this.getLanguage().sendMessage(sender, "end");
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
