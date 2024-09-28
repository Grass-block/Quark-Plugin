package org.atcraftmc.quark.chat;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.util.BukkitSound;
import org.tbstcraft.quark.util.CachedInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    public void onCommand(CommandSender sender, String[] args) {
        if (Objects.equals(args[0], "view")) {
            TaskService.async().run(() -> {
                NBTTagCompound entry = PlayerDataService.getEntry(sender.getName(), this.getFullId());
                StringBuilder sb = new StringBuilder();
                Set<String> keys = new HashSet<>(entry.getTagMap().keySet());
                if (keys.isEmpty()) {
                    this.language.sendMessage(sender, "view-none", sb.toString());
                    return;
                }
                try {
                    for (String s : keys) {
                        String from = s.split("@")[0];
                        String time = SharedObjects.DATE_FORMAT.format(Long.parseLong(s.split("@")[1]));

                        String template = this.getConfig().getString("template");
                        if (template == null) {
                            template = "%s@%s: %s";
                        }
                        sb.append(template.formatted(time, from, entry.getString(s))).append("\n");
                        entry.remove(s);
                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
                PlayerDataService.save(sender.getName());
                this.language.sendMessage(sender, "view", sb.toString());
            });
            return;
        }


        StringBuilder sb = new StringBuilder(32);

        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String content = TextBuilder.EMPTY_COMPONENT + sb + TextBuilder.EMPTY_COMPONENT;
        String recipient = args[0];
        Player recipientPlayer = Bukkit.getPlayerExact(recipient);
        if (recipientPlayer != null) {
            this.language.sendMessage(recipientPlayer, "receive-direct", sender.getName(), content);
            this.language.sendMessage(sender, "send-success", recipient, content);

            BukkitSound.ANNOUNCE.play(recipientPlayer);
            return;
        }

        NBTTagCompound entry = PlayerDataService.getEntry(recipient, this.getFullId());
        String key = sender.getName() + "@" + System.currentTimeMillis();
        entry.setString(key, content);
        this.language.sendMessage(sender, "send-success", recipient, content);
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.addAll(CachedInfo.getAllPlayerNames());
            tabList.add("view");
        } else {
            tabList.add("message");
        }
    }
}
