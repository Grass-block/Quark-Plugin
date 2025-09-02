package org.atcraftmc.quark.utilities;

import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;

import java.util.List;

@SLModule(version = "1.0.0")
@QuarkCommand(name = "ping", playerOnly = true)
public final class PlayerPingCommand extends CommandModule {
    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
        super.enable();
    }

    @Override
    public void disable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
        super.disable();
    }

    public String getPing(CommandSender sender) {
        return PlaceHolderService.PLAYER.get("ping", ((Player) sender));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        PluginMessenger.broadcastListed("proxy-ping:update", List.of(sender));
        MessageAccessor.send(this.getLanguage(), sender, "ping-msg", getPing(sender));
    }
}
