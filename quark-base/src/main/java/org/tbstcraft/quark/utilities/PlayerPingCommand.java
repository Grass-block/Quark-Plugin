package org.tbstcraft.quark.utilities;

import me.gb2022.commons.reflect.Inject;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.data.PlaceHolderStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;

import java.util.HashSet;
import java.util.List;

@QuarkModule(version = "1.0.0")
@QuarkCommand(name = "ping")
public final class PlayerPingCommand extends CommandModule {
    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PlaceHolderStorage.get(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, HashSet.class, (s) -> s.add(this.tip));
        super.enable();
    }

    @Override
    public void disable(){
        PlaceHolderStorage.get(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, HashSet.class, (s) -> s.remove(this.tip));
        super.disable();
    }

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
