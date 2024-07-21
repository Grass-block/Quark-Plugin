package org.tbstcraft.quark.chat;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.tbstcraft.quark.data.ModuleDataService;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.util.container.CachedInfo;

import java.util.List;
import java.util.Objects;

@CommandProvider(ChatMute.MuteCommand.class)
@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(id="chat-mute",version = "1.0.2")
public final class ChatMute extends PackageModule implements Listener {

    @EventHandler
    public void onChatting(AsyncPlayerChatEvent event) {
        this.checkEvent(event.getPlayer(), event);
    }

    @EventHandler
    public void detectCommand(PlayerCommandPreprocessEvent event) {
        if (!(event.getMessage().contains("say") || event.getMessage().contains("tell"))) {
            return;
        }
        this.checkEvent(event.getPlayer(), event);
    }

    public void checkEvent(Player p, Cancellable event) {
        NBTTagCompound tag = ModuleDataService.getEntry(this.getId());
        if (!tag.hasKey(p.getName())) {
            return;
        }
        event.setCancelled(true);
        this.getLanguage().sendMessage(Objects.requireNonNull(p.getPlayer()), "message-banned");
    }

    @QuarkCommand(name = "mute", op = true)
    public static final class MuteCommand extends ModuleCommand<ChatMute> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
           /* UIBuilder.inventory("Test", 54)
                    .close(8, 4)
                    .valueChangerH4(4, 2, 0, 1, 64,
                            ((player, ui, value) -> player.sendMessage("value: " + value)))
                    .command(1,1,BukkitUtil.createStack(Material.GRASS_BLOCK,1,"6"),"say 我是傻逼")
                    .closeable(false)
                    .build(((Player) sender))
                    .show();

            */

            String operation = args[0];
            String target = args[1];
            if (operation.equals("add")) {
                ModuleDataService.getEntry(this.getModuleId()).hasKey(target);
                this.getLanguage().sendMessage(sender, "add", target);
                Player player = PlayerUtil.strictFindPlayer(target);
                NBTTagCompound tag = ModuleDataService.getEntry(this.getModuleId());
                if (!tag.hasKey(target)) {
                    tag.setByte(target, (byte) 0);
                }
                ModuleDataService.save(this.getModuleId());
                if (player != null) {
                    this.getLanguage().sendMessage(player, "add-target", sender.getName());
                }
            }
            if (operation.equals("remove")) {
                NBTTagCompound tag = ModuleDataService.getEntry(this.getModuleId());
                if (tag.hasKey(target)) {
                    tag.remove(target);
                }
                ModuleDataService.save(this.getModuleId());
                this.getLanguage().sendMessage(sender, "remove", target);
                Player player = PlayerUtil.strictFindPlayer(target);
                if (player != null) {
                    this.getLanguage().sendMessage(player, "remove-target", sender.getName());
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("add");
                tabList.add("remove");
                return;
            }
            if (buffer.length != 2) {
                return;
            }
            if (buffer[0].equals("add")) {
                tabList.addAll(CachedInfo.getAllPlayerNames());
            }
            if (buffer[0].equals("remove")) {
                tabList.addAll(ModuleDataService.getEntry(this.getModuleId()).getTagMap().keySet());
            }
        }
    }
}