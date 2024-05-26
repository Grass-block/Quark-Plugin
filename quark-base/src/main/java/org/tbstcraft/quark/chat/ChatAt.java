package org.tbstcraft.quark.chat;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.framework.data.config.Queries;
import org.tbstcraft.quark.framework.module.services.ModuleService;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.util.text.TextBuilder;
import org.tbstcraft.quark.util.text.TextSender;
import org.tbstcraft.quark.util.platform.PlayerUtil;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@ModuleService(ServiceType.EVENT_LISTEN)
@QuarkModule(id = "chat-at",version = "1.0.2")
public final class ChatAt extends PackageModule {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        ConfigurationSection cfg = this.getConfig();

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
            Player p = PlayerUtil.strictFindPlayer(column.replaceFirst("@", ""));
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
            Player p = PlayerUtil.strictFindPlayer(s);
            if (p == null) {
                continue;
            }
            TextSender.sendToTitle(p, TextBuilder.buildComponent(this.generateTitleMessage(titleBuilder.toString(), event.getPlayer())),
                    cfg.getInt("title-fadein"),
                    cfg.getInt("title-stay"),
                    cfg.getInt("title-fadeout"));
            if (cfg.getBoolean("sound")) {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
            }
        }
    }

    public String generateAtMessage(String column) {
        ConfigurationSection cfg = this.getConfig();
        String target = column.replaceFirst("@", "");
        String completedTemplate = Objects.requireNonNull(cfg.getString("at-template")).replace("{player}", target);
        return Queries.GLOBAL_TEMPLATE_ENGINE.handle(completedTemplate);
    }

    public String generateTitleMessage(String msg, Player p) {
        ConfigurationSection cfg = this.getConfig();
        String template = Objects.requireNonNull(cfg.getString("at-title-template"));
        String completedTemplate = template.replace("{player}", p.getName()).replace("{message}", msg);
        return Queries.GLOBAL_TEMPLATE_ENGINE.handle(completedTemplate);
    }
}
