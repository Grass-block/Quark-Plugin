package org.tbstcraft.quark.utilities;

import me.gb2022.commons.reflect.Inject;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.data.PlaceHolderStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@QuarkCommand(name = "sprint", playerOnly = true)
@QuarkModule(version = "1.0")
public final class ForceSprint extends CommandModule {
    private final Set<String> players = new HashSet<>();

    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        super.enable();
        PlaceHolderStorage.get(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, HashSet.class, (s) -> s.add(this.tip));
        TaskService.timerTask("sprint:tick", 10, 10, () -> {
            for (String player : this.players) {
                Objects.requireNonNull(PlayerUtil.strictFindPlayer(player)).setSprinting(true);
            }
        });
    }

    @Override
    public void disable() {
        super.disable();
        PlaceHolderStorage.get(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, HashSet.class, (s) -> s.remove(this.tip));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (this.players.contains(sender.getName())) {
            this.players.remove(sender.getName());
            getLanguage().sendMessage(sender,"disable");
        } else {
            this.players.add(sender.getName());
            getLanguage().sendMessage(sender,"enable");
        }
    }
}
