package org.tbstcraft.quark.chat;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.foundation.text.TextSender;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(id = "chat-at", version = "1.2")
public final class ChatAt extends PackageModule {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Players.addChatTabOption(player, "@" + event.getPlayer().getName());
            Players.addChatTabOption(event.getPlayer(), "@" + player.getName());
        }
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
                    cfg.getInt("title-fadein"),
                    cfg.getInt("title-stay"),
                    cfg.getInt("title-fadeout"));
            if (cfg.getBoolean("sound")) {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
            }
        }
    }

    public String generateAtMessage(String column) {
        var cfg = this.getConfig();
        String target = column.replaceFirst("@", "");
        String completedTemplate = Objects.requireNonNull(cfg.getString("at-template")).replace("{player}", target);
        return PlaceHolderService.format(completedTemplate);
    }

    public String generateTitleMessage(String msg, Player p) {
        var cfg = this.getConfig();
        String template = Objects.requireNonNull(cfg.getString("at-title-template"));
        String completedTemplate = template.replace("{player}", p.getName()).replace("{message}", msg);
        return PlaceHolderService.format(completedTemplate);
    }
}
