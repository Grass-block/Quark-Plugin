package org.tbstcraft.quark.display;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.framework.command.CommandRegistry;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.config.Language;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.service.task.TaskService;

import java.util.List;
import java.util.function.Function;

@QuarkModule(version = "0.3.0")
@EventListener
@CommandRegistry(ChatAnnounce.HintCommand.class)
public final class ChatAnnounce extends PackageModule {
    private long index;
    private boolean freeze;

    @Override
    public void enable() {
        this.tick();
        int p = this.getConfig().getInt("period");
        TaskService.timerTask("chat-announce:tick", p, p, this::tick);
    }

    @Override
    public void disable() {
        TaskService.cancelTask("chat-announce:tick");
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    private void onChat(AsyncPlayerChatEvent event) {
        this.freeze = false;
    }


    public void tick() {
        if (this.freeze) {
            return;
        }
        this.index++;
        for (Player p : Bukkit.getOnlinePlayers()) {
            sendMessage(p);
        }
        this.freeze = true;
    }

    private List<String> getContents(CommandSender sender) {
        return this.getLanguage().getMessageList(Language.getLocale(sender), "content");
    }

    private void sendMessage(CommandSender sender) {
        Function<String, String> processor = (s) ->
                s.formatted(this.getContents(sender).get((int) (this.index % this.getContents(sender).size())));
        this.getLanguage().sendUI(sender, this.getConfig(), "ui", processor);
    }

    @QuarkCommand(name = "chat-hint")
    public static final class HintCommand extends ModuleCommand<ChatAnnounce> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.getModule().sendMessage(sender);
            this.getModule().index++;
        }
    }
}
