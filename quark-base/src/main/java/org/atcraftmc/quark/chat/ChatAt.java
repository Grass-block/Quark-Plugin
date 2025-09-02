package org.atcraftmc.quark.chat;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.atcraftmc.starlight.foundation.platform.Players;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@AutoRegister(ServiceType.EVENT_LISTEN)
@SLModule(id = "chat-at", version = "1.2")
public final class ChatAt extends PackageModule {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Players.addChatTabOption(player, "@" + event.getPlayer().getName());
            Players.addChatTabOption(event.getPlayer(), "@" + player.getName());
        }
        Players.addChatTabOption(event.getPlayer(), "@all");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Players.removeChatTabOption(player, "@" + event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        var cfg = this.getConfig();

        String msg = event.getMessage();

        StringBuilder titleBuilder = new StringBuilder();
        StringBuilder msgBuilder = new StringBuilder();
        Set<String> targets = new HashSet<>();

        for (String column : msg.split(" ")) {
            if (!column.startsWith("@")) {
                titleBuilder.append(column).append(" ");
                msgBuilder.append(column).append(" ");
                continue;
            }

            if (column.equals("@all")) {
                msgBuilder.append(this.generateAtMessage("all")).append(" ");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    targets.add(p.getName());
                }
                continue;
            }
            Player p = Bukkit.getPlayerExact(column.replaceFirst("@", ""));
            if (!Bukkit.getOnlinePlayers().contains(p)) {
                continue;
            }
            if (p == null) {
                continue;
            }
            msgBuilder.append(generateAtMessage(column)).append(" ");
            targets.add(p.getName());
        }

        event.setMessage(msgBuilder.toString());

        targets.remove(event.getPlayer().getName());

        for (String s : targets) {
            Player p = Bukkit.getPlayerExact(s);
            if (p == null) {
                continue;
            }
            TextSender.subtitle(p, TextBuilder.buildComponent(this.generateTitleMessage(titleBuilder.toString(), event.getPlayer())),
                    cfg.value("title-fadein").intValue(),
                    cfg.value("title-stay").intValue(),
                    cfg.value("title-fadeout").intValue());
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
}
