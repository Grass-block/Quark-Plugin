package org.atcraftmc.quark.utilities;

import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.HashSet;
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
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
        TaskService.async().timer("sprint:tick", 10, 10, () -> {
            for (String player : this.players) {
                Player p = Bukkit.getPlayerExact(player);
                if (p == null) {
                    continue;
                }

                TaskService.entity(p).run(() -> p.setSprinting(true));
            }
        });
    }

    @Override
    public void disable() {
        super.disable();
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));

        TaskService.async().cancel("sprint:tick");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (this.players.contains(sender.getName())) {
            this.players.remove(sender.getName());
            getLanguage().sendMessage(sender, "disable");
        } else {
            this.players.add(sender.getName());
            getLanguage().sendMessage(sender, "enable");
        }
    }
}
