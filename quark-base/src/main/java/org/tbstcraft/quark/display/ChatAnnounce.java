package org.tbstcraft.quark.display;

import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.framework.command.CommandProvider;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import me.gb2022.commons.reflect.AutoRegister;
import org.tbstcraft.quark.framework.data.language.LanguageEntry;
import org.tbstcraft.quark.framework.data.language.Language;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.service.base.task.TaskService;

import java.util.List;
import java.util.function.Function;

@QuarkModule(version = "0.3.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(ChatAnnounce.HintCommand.class)
public final class ChatAnnounce extends PackageModule {
    private long index;
    private boolean freeze;

    @Inject
    private LanguageEntry language;

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
        return this.language.getMessageList(Language.locale(sender), "content");
    }

    private void sendMessage(CommandSender sender) {
        Function<String, String> processor = (s) ->
                s.formatted(this.getContents(sender).get((int) (this.index % this.getContents(sender).size())));



        this.language.sendTemplate(sender,Language.generateTemplate(this.getConfig(), "ui", processor));
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
