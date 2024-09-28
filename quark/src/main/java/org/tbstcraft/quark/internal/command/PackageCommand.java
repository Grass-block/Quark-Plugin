package org.tbstcraft.quark.internal.command;

import me.gb2022.commons.TriState;
import net.kyori.adventure.text.Component;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.packages.IPackage;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.util.ObjectOperationResult;

import java.util.*;

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
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "enable", "disable", "list");
        suggestion.matchArgument(0, "list", (c) -> c.suggest(1, "<search meta>"));
        suggestion.matchArgument(0, "list", (c) -> c.suggest(1, PackageManager.getInstance().getPackages().keySet()));
        suggestion.matchArgument(0, "enable", (c) -> c.suggest(1, PackageManager.getIdsByStatus(TriState.FALSE)));
        suggestion.matchArgument(0, "disable", (c) -> c.suggest(1, PackageManager.getIdsByStatus(TriState.TRUE)));
    }

    @Override
    public void execute(CommandExecution context) {
        var sender = context.getSender();
        var id = !context.hasArgumentAt(1) ? null : context.requireArgumentAt(1);

        switch (context.requireEnum(0, "list", "info", "enable", "disable", "reload", "enable-all", "disable-all", "reload-all")) {
            case "list" -> list(sender, !context.hasArgumentAt(1) ? "" : context.requireArgumentAt(1));
            case "enable-all" -> {
                PackageManager.enableAllPackages();
                this.getLanguage().sendMessage(sender, "enable-all");
            }
            case "disable-all" -> {
                PackageManager.disableAllPackages();
                this.getLanguage().sendMessage(sender, "disable-all");
            }
            case "enable" -> sendMessage(sender, messageId(PackageManager.enablePackage(id), "enable"), id);
            case "disable" -> sendMessage(sender, messageId(PackageManager.disablePackage(id), "disable"), id);
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
                if (PackageManager.getPackageStatus(id) == TriState.FALSE) {
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

    private Component buildModuleInfo(IPackage m) {
        var state = PackageManager.isPackageEnabled(m.getId()) ? "&aE" : "&cD";
        var owner = m.getOwner().getName();
        var ownerVer = m.getOwner().getDescription().getVersion();
        var line = "&f[%s&f]%s &7[%s]".formatted(state, m.getId(), owner + ":" + ownerVer);

        return Component.text(ChatColor.translateAlternateColorCodes('&', line));
    }

    private void list(CommandSender sender, String prefix) {
        var nodes = PackageManager.getInstance()
                .getPackages()
                .values()
                .stream()
                .sorted(Comparator.comparing(m -> m.getOwner().getName()))
                .filter((m) -> m.getId().contains(prefix))
                .toList();
        getLanguage().sendMessage(sender, "list", "");
        for (var meta : nodes) {
            Players.sendMessage(sender, buildModuleInfo(meta));
        }
    }

    @Override
    public String getLanguageNamespace() {
        return "package";
    }
}
