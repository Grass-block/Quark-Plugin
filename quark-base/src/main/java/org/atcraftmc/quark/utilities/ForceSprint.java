package org.atcraftmc.quark.utilities;

import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.core.TaskService;

import java.util.HashSet;
import java.util.Set;

@QuarkCommand(name = "sprint", playerOnly = true)
@SLModule(version = "1.0")
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
            MessageAccessor.send(this.getLanguage(), sender, "disable");
        } else {
            this.players.add(sender.getName());
            MessageAccessor.send(this.getLanguage(), sender, "enable");
        }
    }
}
