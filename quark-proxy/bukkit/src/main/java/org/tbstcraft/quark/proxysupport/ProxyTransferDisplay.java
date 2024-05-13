package org.tbstcraft.quark.proxysupport;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.command.CommandRegistry;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.service.proxy.ChannelHandler;
import org.tbstcraft.quark.service.proxy.ProxyChannel;
import org.tbstcraft.quark.util.api.PlayerUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

@CommandRegistry(ProxyTransferDisplay.STPDisplayCommand.class)
@QuarkModule(id = "proxy-transfer-display")
public class ProxyTransferDisplay extends PackageModule implements ChannelHandler {

    @Override
    public void onMessageReceived(String channelId, byte[] data, ProxyChannel channel) {
        if (!channelId.startsWith("quark:bc.stp")) {
            return;
        }
        String str = new String(data, StandardCharsets.UTF_8);
        String player = str.split(";")[0];
        String serverName = str.split(";")[1];
        switch (channelId) {
            case "quark:bc.stp.join" -> {
                displayTransferComplete(player, serverName);
            }
            case "quark:bc.stp.leave" -> {
            }
        }
    }

    public void displayTransferComplete(String name, String currentServer) {
        Player player = PlayerUtil.strictFindPlayer(name);
        if (player == null) {
            return;
        }
        this.getLanguage().sendMessageTo(player, "tp_completed_user", currentServer);
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1, 1);
    }

    @QuarkCommand(name = "stp_display", op = true)
    public static final class STPDisplayCommand extends ModuleCommand<ProxyTransferDisplay> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            switch (args[0]) {
                case "complete" -> this.getModule().displayTransferComplete(sender.getName(), args[1]);
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("complete");
                tabList.add("forward");
                tabList.add("leave");
                tabList.add("join");
            }
        }
    }
}
