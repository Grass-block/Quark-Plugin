package org.tbstcraft.quark.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.service.ModuleDataService;
import org.tbstcraft.quark.util.nbt.NBTTagCompound;

import java.util.List;
import java.util.Objects;

@QuarkModule
public final class ChatMute extends PluginModule implements Listener {
    CommandHandler commandHandler = new CommandHandler(this);

    @Override
    public void onEnable() {
        this.registerListener();
        this.registerCommand(this.commandHandler);
    }

    @Override
    public void onDisable() {
        this.unregisterCommand(this.commandHandler);
        this.unregisterListener();
    }


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
        this.getLanguage().sendMessageTo(Objects.requireNonNull(p.getPlayer()), "message_banned");
    }

    @QuarkCommand(name = "mute", op = true)
    static final class CommandHandler extends ModuleCommand<ChatMute> {
        private CommandHandler(ChatMute module) {
            super(module);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            PluginModule m = this.getModule();
            String operation = args[0];
            String target = args[1];
            if (operation.equals("add")) {
                ModuleDataService.getEntry(this.getModuleId()).hasKey(target);
                m.getLanguage().sendMessageTo(sender, "add", target);
                Player player = Bukkit.getPlayer(target);
                NBTTagCompound tag = ModuleDataService.getEntry(this.getModuleId());
                if (!tag.hasKey(target)) {
                    tag.setByte(target, (byte) 0);
                }
                ModuleDataService.save(this.getModuleId());
                if (player != null) {
                    m.getLanguage().sendMessageTo(player, "add_target", sender.getName());
                }
            }
            if (operation.equals("remove")) {
                NBTTagCompound tag = ModuleDataService.getEntry(this.getModuleId());
                if (tag.hasKey(target)) {
                    tag.remove(target);
                }
                ModuleDataService.save(this.getModuleId());
                m.getLanguage().sendMessageTo(sender, "remove", target);
                Player player = Bukkit.getPlayer(target);
                if (player != null) {
                    m.getLanguage().sendMessageTo(player, "remove_target", sender.getName());
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
            if (args.length == 1) {
                tabList.add("add");
                tabList.add("remove");
                return;
            }
            if (args.length != 2) {
                return;
            }
            if (args[0].equals("add")) {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    tabList.add(p.getName());
                }
            }
            if (args[0].equals("remove")) {
                tabList.addAll(ModuleDataService.getEntry(this.getModuleId()).getTagMap().keySet());
            }
        }
    }
}