package org.tbstcraft.quark.proxysupport;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.permission.PermissionService;

import java.util.List;
import java.util.Objects;

/**
* requires mod Vanilla CrossServer API Mod to be installed on client.
*/
@CommandProvider(ForgeServerTeleportation.HubCommand.class)
@QuarkCommand(name = "teleport-server", permission = "+quark.stp")
@QuarkModule(version = "1.0.0")
public final class ForgeServerTeleportation extends CommandModule {
    public static final String PREFIX = "{$client:connect}";

    @Override
    public void enable() {
        super.enable();
        PermissionService.createPermission("-quark.stp.other");
    }

    @Override
    public void disable() {
        super.disable();
        PermissionService.deletePermission("-quark.stp.other");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        ConfigurationSection section = this.getConfig().getSection("servers");

        String host = args[0];

        if (Objects.requireNonNull(section).contains(args[0])) {
            host = section.getString(args[0]);
        }

        if (args.length == 1) {
            sender.sendMessage(PREFIX + host);
        } else {
            if (!sender.hasPermission("quark.stp.other")) {
                sendPermissionMessage(sender);
                return;
            }
            Objects.requireNonNull(Bukkit.getPlayerExact(args[1])).sendMessage(PREFIX + host);
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        ConfigurationSection section = this.getConfig().getSection("servers");
        if (buffer.length == 1) {
            tabList.addAll(Objects.requireNonNull(section).getKeys(false));
        }
        if (buffer.length == 2) {
            tabList.addAll(Players.getAllOnlinePlayerNames());
        }
    }

    @QuarkCommand(name = "hub", playerOnly = true)
    public static final class HubCommand extends ModuleCommand<ForgeServerTeleportation> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.getModule().onCommand(sender, new String[]{"hub"});
        }
    }
}
