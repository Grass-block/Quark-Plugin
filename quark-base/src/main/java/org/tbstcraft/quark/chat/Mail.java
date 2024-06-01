package org.tbstcraft.quark.chat;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.services.ModuleService;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.data.PlayerDataService;
import org.tbstcraft.quark.service.base.task.TaskService;
import org.tbstcraft.quark.util.BukkitSound;
import org.tbstcraft.quark.util.container.CachedInfo;
import org.tbstcraft.quark.util.platform.PlayerUtil;
import org.tbstcraft.quark.util.text.TextBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@QuarkCommand(name = "mail", playerOnly = true)
@QuarkModule(id = "mail",version = "1.0.0")
@ModuleService(ServiceType.EVENT_LISTEN)
public final class Mail extends CommandModule {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        TaskService.asyncTask(() -> {
            Player sender = event.getPlayer();
            NBTTagCompound entry = PlayerDataService.getEntry(sender.getName(), this.getFullId());
            int size = entry.getTagMap().keySet().size();
            if (size == 0) {
                return;
            }
            this.getLanguage().sendMessageTo(sender, "view-hint", size);
        });
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (Objects.equals(args[0], "view")) {
            TaskService.asyncTask(() -> {
                NBTTagCompound entry = PlayerDataService.getEntry(sender.getName(), this.getFullId());
                StringBuilder sb = new StringBuilder();
                Set<String> keys = new HashSet<>(entry.getTagMap().keySet());
                if (keys.isEmpty()) {
                    this.getLanguage().sendMessageTo(sender, "view-none", sb.toString());
                    return;
                }
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
                PlayerDataService.save(sender.getName());
                this.getLanguage().sendMessageTo(sender, "view", sb.toString());
            });
        }


        StringBuilder sb = new StringBuilder(32);

        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String content = TextBuilder.EMPTY_COMPONENT + sb + TextBuilder.EMPTY_COMPONENT;
        String recipient = args[0];
        Player recipientPlayer = PlayerUtil.strictFindPlayer(recipient);
        if (recipientPlayer != null) {
            getLanguage().sendMessageTo(recipientPlayer, "receive-direct", sender.getName(), content);
            getLanguage().sendMessageTo(sender, "send-success", recipient, content);
            BukkitSound.ANNOUNCE.play(recipientPlayer);
            return;
        }

        NBTTagCompound entry = PlayerDataService.getEntry(recipient, this.getFullId());
        String key = sender.getName() + "@" + System.currentTimeMillis();
        entry.setString(key, content);
        getLanguage().sendMessageTo(sender, "send-success", recipient, content);
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.addAll(CachedInfo.getAllPlayerNames());
            tabList.add("view");
        }else{
            tabList.add("message");
        }
    }
}
