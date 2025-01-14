package org.atcraftmc.quark.chat;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.util.BukkitSound;

import java.util.HashSet;

@QuarkCommand(name = "mail", playerOnly = true)
@QuarkModule(id = "mail", version = "1.0.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class Mail extends CommandModule {

    @Inject
    private LanguageEntry language;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        TaskService.async().run(() -> {
            Player sender = event.getPlayer();
            NBTTagCompound entry = PlayerDataService.getEntry(sender.getName(), this.getFullId());
            int size = entry.getTagMap().keySet().size();
            if (size == 0) {
                return;
            }
            this.language.sendMessage(sender, "view-hint", size);
        });
    }

    @Override
    public void execute(CommandExecution context) {
        var sender = context.getSender();

        if (context.requireArgumentAt(0).equals("view")) {
            TaskService.async().run(() -> {
                var entry = PlayerDataService.getEntry(sender.getName(), this.getFullId());
                var sb = new StringBuilder();
                var keys = new HashSet<>(entry.getTagMap().keySet());

                if (keys.isEmpty()) {
                    this.language.sendMessage(sender, "view-none", sb.toString());
                    return;
                }

                for (String s : keys) {
                    var template = this.getConfig().getString("template");
                    if (template == null) {
                        template = "%s@%s: %s";
                    }

                    try {
                        var from = s.split("@")[0];
                        var time = SharedObjects.DATE_FORMAT.format(Long.parseLong(s.split("@")[1]));

                        sb.append(template.formatted(time, from, entry.getString(s))).append("\n");
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                    }

                    entry.remove(s);
                }

                PlayerDataService.save(sender.getName());
                this.language.sendMessage(sender, "view", sb.toString());
            });

            var content = TextBuilder.EMPTY_COMPONENT + context.requireRemainAsParagraph(0, true) + TextBuilder.EMPTY_COMPONENT;
            var recipient = context.requireOfflinePlayer(0);

            if (recipient.isOnline()) {
                this.language.sendMessage(recipient, "receive-direct", sender.getName(), content);
                this.language.sendMessage(sender, "send-success", recipient, content);

                if (getConfig().getBoolean("sound")) {
                    BukkitSound.ANNOUNCE.play(recipient.getPlayer());
                }

                return;
            }

            var entry = PlayerDataService.getEntry(recipient.getName(), this.getFullId());
            var key = sender.getName() + "@" + System.currentTimeMillis();
            entry.setString(key, content);

            this.language.sendMessage(sender, "send-success", recipient, content);
        }
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggestPlayers(0);
        suggestion.suggest(0, "view");
        suggestion.suggest(1, "[message...]");
    }
}
