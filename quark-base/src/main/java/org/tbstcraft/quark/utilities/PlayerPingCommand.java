package org.tbstcraft.quark.utilities;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;

import java.util.List;

@QuarkModule(version = "1.0.0")
@QuarkCommand(name = "ping")
public final class PlayerPingCommand extends CommandModule {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        int ping = PlayerUtil.getPing((Player) sender);
        StringBuilder sb = new StringBuilder();
        if (ping < 75) {
            sb.append(ChatColor.GREEN);
        } else if (ping < 250) {
            sb.append(ChatColor.YELLOW);
        } else {
            sb.append(ChatColor.RED);
        }
        sb.append(ping);
        sb.append("{#reset}");

        this.getLanguage().sendMessage(sender, "ping-msg", sb.toString());
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
    }
}
