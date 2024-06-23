package org.tbstcraft.quark.internal.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.util.ObjectOperationResult;
import org.tbstcraft.quark.util.ObjectStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@QuarkCommand(name = "package", permission = "-quark.packages")
public final class PackageCommand extends CoreCommand {
    static String messageId(ObjectOperationResult result, String success) {
        return switch (result) {
            case SUCCESS -> success;
            case NOT_FOUND -> "not-found";
            case ALREADY_OPERATED -> "already-op";
            case INTERNAL_ERROR -> "internal-error";
            case BLOCKED_INTERNAL -> "blocked-internal";
        };
    }

    private void sendMessage(CommandSender sender, String id, Object... fmt) {
        this.getLanguage().sendMessage(sender, id, fmt);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "list" -> this.listPackages(sender);
            case "enable-all" -> {
                PackageManager.enableAllPackages();
                this.getLanguage().sendMessage(sender, "enable-all");
            }
            case "disable-all" -> {
                PackageManager.disableAllPackages();
                this.getLanguage().sendMessage(sender, "disable-all");
            }
            case "enable" -> sendMessage(sender, messageId(PackageManager.enablePackage(args[1]), "enable"), args[1]);
            case "disable" ->
                    sendMessage(sender, messageId(PackageManager.disablePackage(args[1]), "disable"), args[1]);
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("enable");
            tabList.add("disable");
            tabList.add("enable-all");
            tabList.add("disable-all");
            tabList.add("list");
            return;
        }
        if (buffer.length != 2 || buffer[0].contains("-all") || buffer[0].equals("list")) {
            return;
        }

        if (Objects.equals(buffer[0], "reload") || Objects.equals(buffer[0], "disable")) {
            tabList.addAll(PackageManager.getIdsByStatus(ObjectStatus.ENABLED));
        } else {
            tabList.addAll(PackageManager.getIdsByStatus(ObjectStatus.DISABLED));
        }
        if (tabList.isEmpty()) {
            tabList.add("(not found)");
        }
    }

    private void listPackages(CommandSender sender) {
        StringBuilder sb = new StringBuilder();
        HashMap<String, List<String>> map = new HashMap<>();
        for (String s : PackageManager.getAllPackages().keySet().stream().sorted().toList()) {
            String namespace = PackageManager.getPackage(s).getOwner().getName();
            if (!map.containsKey(namespace)) {
                map.put(namespace, new ArrayList<>());
            }
            map.get(namespace).add(s);
        }

        for (String namespace : map.keySet()) {
            List<String> list = map.get(namespace);
            sb.append(ChatColor.GOLD)
                    .append(namespace)
                    .append("@")
                    .append(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(namespace)).getDescription().getVersion())
                    .append("(")
                    .append(list.size())
                    .append("):\n");
            for (String id : list) {
                sb.append(ChatColor.RESET).append(" - ");
                if (PackageManager.getPackageStatus(id) == ObjectStatus.ENABLED) {
                    sb.append(ChatColor.GREEN);
                } else {
                    sb.append(ChatColor.GRAY);
                }
                sb.append(id);
                sb.append('\n');
            }
        }

        this.getLanguage().sendMessage(sender, "list", sb.toString());
    }


    @Override
    public String getLanguageNamespace() {
        return "package";
    }
}
