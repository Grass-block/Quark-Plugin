package org.atcraftmc.starlight.utilities;

import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;
import org.atcraftmc.starlight.framework.module.SLCommandModule;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@ApplicationModule(id="player-ping-command",version = "1.0.0")
@QuarkCommand(name = "ping", playerOnly = true)
public final class PlayerPingCommand extends SLCommandModule {
    @Inject("tip")
    private LanguageItem tip;

    @Inject
    private LanguageEntry language;

    @Override
    public void enable() throws Exception {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
        super.enable();
    }

    @Override
    public void disable() throws Exception {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
        super.disable();
    }

    public String getPing(CommandSender sender) {
        return PlaceHolderService.PLAYER.get("ping", ((Player) sender));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        PluginMessenger.broadcastListed("proxy-ping:update", List.of(sender));
        MessageAccessor.send(this.language, sender, "ping-msg", getPing(sender));
    }
}
