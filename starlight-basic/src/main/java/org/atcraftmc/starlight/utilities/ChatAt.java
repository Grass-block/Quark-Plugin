package org.atcraftmc.starlight.utilities;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.platform.Players;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.Registers;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Objects;

@AutoRegister(Registers.BUKKIT_EVENT)
@SLModule(id = "chat-at", version = "1.2")
@CommandProvider(ChatAt.AtCommand.class)
public final class ChatAt extends PackageModule {

    @Override
    public void enable() throws Exception {
        for (var player : Bukkit.getOnlinePlayers()) {
            registerPlayerCompletion(player);
        }
    }

    @Override
    public void disable() throws Exception {
        for (var player : Bukkit.getOnlinePlayers()) {
            unregisterPlayerCompletion(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        registerPlayerCompletion(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        unregisterPlayerCompletion(event.getPlayer());
    }

    public void registerPlayerCompletion(Player p) {
        for (var player : Bukkit.getOnlinePlayers()) {
            Players.addChatTabOption(player, "@" + p.getName());
            Players.addChatTabOption(p, "@" + player.getName());
        }
        Players.addChatTabOption(p, "@all");
    }

    public void unregisterPlayerCompletion(Player p) {
        for (var player : Bukkit.getOnlinePlayers()) {
            Players.removeChatTabOption(player, "@" + p.getName());
            Players.removeChatTabOption(p, "@" + player.getName());
        }
        Players.removeChatTabOption(p, "@all");
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        var cfg = this.getConfig();
        var msg = event.getMessage();

        var titleBuilder = new StringBuilder();
        var msgBuilder = new StringBuilder();
        var targets = new HashSet<String>();

        for (var column : msg.split(" ")) {
            if (!column.startsWith("@")) {
                titleBuilder.append(column).append(" ");
                msgBuilder.append(column).append(" ");
                continue;
            }

            if (column.equals("@all")) {
                msgBuilder.append(this.generateAtMessage("all")).append(" ");
                for (var p : Bukkit.getOnlinePlayers()) {
                    targets.add(p.getName());
                }
                continue;
            }
            var p = Bukkit.getPlayerExact(column.replaceFirst("@", ""));
            if (!Bukkit.getOnlinePlayers().contains(p)) {
                msgBuilder.append(column);
                continue;
            }
            if (p == null) {
                msgBuilder.append(column);
                continue;
            }
            msgBuilder.append(generateAtMessage(column)).append(" ");
            targets.add(p.getName());
        }

        event.setMessage(msgBuilder.toString());

        targets.remove(event.getPlayer().getName());

        for (var s : targets) {
            var p = Bukkit.getPlayerExact(s);
            if (p == null) {
                continue;
            }
            TextSender.subtitle(p, TextBuilder.buildComponent(this.generateTitleMessage(titleBuilder.toString(), event.getPlayer())),
                                cfg.value("title-fadein").intValue(),
                                cfg.value("title-stay").intValue(),
                                cfg.value("title-fadeout").intValue()
            );
            if (cfg.value("sound").bool()) {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
            }
        }
    }

    public String generateAtMessage(String column) {
        var cfg = this.getConfig();
        var target = column.replaceFirst("@", "");
        var completedTemplate = cfg.value("at-template").string().replace("{player}", target);
        return PlaceHolderService.format(completedTemplate);
    }

    public String generateTitleMessage(String msg, Player p) {
        var cfg = this.getConfig();
        var template = Objects.requireNonNull(cfg.value("at-title-template").string());
        var completedTemplate = template.replace("{player}", p.getName()).replace("{message}", msg);
        return PlaceHolderService.format(completedTemplate);
    }


    @QuarkCommand(name = "at", permission = "+starlight.chat.at")
    public static final class AtCommand extends ModuleCommand<ChatAt> {
        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, "all");
            suggestion.suggestOnlinePlayers(0);
        }

        @Override
        public void execute(CommandExecution context) {
            var target = context.requireArgumentAt(0);
            var payload = context.requireRemainAsParagraph(1, false);

            context.requireSenderAsPlayer().chat("@" + target + " " + payload);
        }
    }
}
