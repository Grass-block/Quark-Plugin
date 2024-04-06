package org.tbstcraft.quark.contents;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.CommandModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.util.BukkitSound;
import org.tbstcraft.quark.util.CachedInfo;
import org.tbstcraft.quark.util.api.PlayerUtil;

import java.util.*;

@QuarkModule(version = "1.0.2")
@QuarkCommand(name = "tpa", playerOnly = true)
public final class TPA extends CommandModule {
    private final HashMap<String, Set<String>> requests = new HashMap<>();

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Set<String> list = requests.get(sender.getName());
        if (list == null) {
            list = new HashSet<>();
            this.requests.put(sender.getName(), list);
        }

        if (Objects.equals(args[0], "list")) {
            StringBuilder sb = new StringBuilder();
            for (String s : getRequestList(sender.getName())) {
                sb.append(s).append("\n");
            }
            getLanguage().sendMessageTo(sender, "list", sb);
        }

        String request = sender.getName();
        String target = args[1];

        Player requestPlayer = ((Player) sender);
        Player targetPlayer = PlayerUtil.strictFindPlayer(target);

        if (targetPlayer == null || !Bukkit.getOnlinePlayers().contains(targetPlayer)) {
            getRequestList(request).remove(target);
            getLanguage().sendMessageTo(sender, "player_not_found");
        }

        switch (args[0]) {
            case "request" -> {
                getRequestList(target).add(request);

                getLanguage().sendMessageTo(requestPlayer, "send", target);
                getLanguage().sendMessageTo(targetPlayer, "send_announce", request, request, request);
                BukkitSound.ANNOUNCE.play(targetPlayer);
            }
            case "accept" -> {
                if (!getRequestList(sender.getName()).contains(target)) {
                    getLanguage().sendMessageTo(sender, "player_not_found");
                }
                getRequestList(sender.getName()).remove(target);
                PlayerUtil.teleport(targetPlayer, requestPlayer.getLocation());
                getLanguage().sendMessageTo(requestPlayer, "accept", target);
                getLanguage().sendMessageTo(targetPlayer, "accepted", request);
                BukkitSound.WARP.play(targetPlayer);
            }
            case "deny" -> {
                if (!getRequestList(sender.getName()).contains(target)) {
                    getLanguage().sendMessageTo(sender, "player_not_found");
                }
                getRequestList(request).remove(target);

                getLanguage().sendMessageTo(requestPlayer, "deny", target);
                getLanguage().sendMessageTo(targetPlayer, "denied", request);
                BukkitSound.DENY.play(targetPlayer);
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("request");
            tabList.add("accept");
            tabList.add("deny");
            tabList.add("list");
        }
        if (buffer.length == 2) {
            switch (buffer[0]) {
                case "accept", "deny" -> {
                    Set<String> list = requests.putIfAbsent(sender.getName(), new HashSet<>());
                    if (list == null) {
                        return;
                    }
                    tabList.addAll(list);
                }
                case "request" -> tabList.addAll(CachedInfo.getOnlinePlayerNames());
            }
        }
    }

    public Set<String> getRequestList(String name) {
        Set<String> list = requests.get(name);
        if (list == null) {
            list = new HashSet<>();
            this.requests.put(name, list);
        }
        return list;
    }
}
