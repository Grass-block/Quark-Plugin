package org.atcraftmc.quark.chat;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.data.storage.StorageTable;
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
        tryRemindPlayer(event.getPlayer());
    }

    private void tryRemindPlayer(Player player) {
        TaskService.async().run(() -> {
            var entry = data(player);
            int size = entry.getTagMap().keySet().size();
            if (size == 0) {
                return;
            }
            this.language.sendMessage(player, "view-hint", size);
        });
    }

    private void view(CommandSender sender, StorageTable data) {
        var sb = new StringBuilder();
        var keys = new HashSet<>(data.getTagMap().keySet());

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

                sb.append(template.formatted(time, from, data.getString(s))).append("\n");
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }

            data.remove(s);
        }

        data.save();
        this.language.sendMessage(sender, "view", sb.toString());
    }

    private void send(OfflinePlayer recipient, CommandSender sender, String content) {
        if (recipient.isOnline()) {
            if (getConfig().getBoolean("sound")) {
                BukkitSound.ANNOUNCE.play(recipient.getPlayer());
            }

            if (this.getConfig().getBoolean("send-direct-to-online-player")) {
                this.language.sendMessage(recipient, "receive-direct", sender.getName(), content);
                this.language.sendMessage(sender, "send-success", recipient, content);
                return;
            }
        }

        var entry = data(recipient);
        var key = sender.getName() + "@" + System.currentTimeMillis();

        entry.setString(key, content);
        entry.save();

        this.language.sendMessage(sender, "send-success", recipient, content);

        if (recipient.isOnline()) {
            tryRemindPlayer(recipient.getPlayer());
        }

    }

    @Override
    public void execute(CommandExecution context) {
        var sender = context.getSender();

        if (context.requireArgumentAt(0).equals("view")) {
            var entry = data(context.requireSenderAsPlayer());
            TaskService.async().run(() -> view(context.getSender(), entry));
        }

        var content = TextBuilder.EMPTY_COMPONENT + context.requireRemainAsParagraph(0, true) + TextBuilder.EMPTY_COMPONENT;
        var recipient = context.requireOfflinePlayer(0);

        send(recipient, sender, content);
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggestPlayers(0);
        suggestion.suggest(0, "view");
        suggestion.suggest(1, "[message...]");
    }


    private StorageTable data(OfflinePlayer player) {
        var data = PlayerDataService.get(player.getName());

        if (!data.hasKey(this.getFullId())) {
            data.setCompoundTag(this.getFullId(), new NBTTagCompound());
        }

        return data.getTable(this.getFullId());
    }
}
