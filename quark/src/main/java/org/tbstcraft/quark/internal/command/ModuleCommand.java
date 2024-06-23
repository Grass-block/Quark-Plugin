package org.tbstcraft.quark.internal.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.module.ModuleManager;
import org.tbstcraft.quark.util.ObjectOperationResult;
import org.tbstcraft.quark.util.ObjectStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@QuarkCommand(name = "module", permission = "-quark.module")
public final class ModuleCommand extends CoreCommand {
    static String messageId(ObjectOperationResult result, String success) {
        return switch (result) {
            case SUCCESS -> success;
            case NOT_FOUND -> "not-found";
            case ALREADY_OPERATED -> "already-op";
            case INTERNAL_ERROR -> "internal-error";
            case BLOCKED_INTERNAL -> "blocked-internal";
        };
    }

    private void sendMessage(CommandSender sender, String id, String mid) {
        AbstractModule module = ModuleManager.getModule(mid);
        this.getLanguage().sendMessage(sender, id, module.getDisplayName(Language.locale(sender)));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "list" -> this.listModules(sender);
            case "enable-all" -> {
                ModuleManager.enableAllModules();
                this.getLanguage().sendMessage(sender, "enable-all");
            }
            case "disable-all" -> {
                ModuleManager.disableAllModules();
                this.getLanguage().sendMessage(sender, "disable-all");
            }
            case "reload-all" -> {
                ModuleManager.reloadAllModules();
                this.getLanguage().sendMessage(sender, "reload-all");
            }
            case "enable" -> sendMessage(sender, messageId(ModuleManager.enableModule(args[1]), "enable"), args[1]);
            case "disable" -> sendMessage(sender, messageId(ModuleManager.disableModule(args[1]), "disable"), args[1]);
            case "reload" -> sendMessage(sender, messageId(ModuleManager.reloadModule(args[1]), "reload"), args[1]);
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("list");
            tabList.add("enable");
            tabList.add("disable");
            tabList.add("reload");
            tabList.add("enable-all");
            tabList.add("disable-all");
            tabList.add("reload-all");
            return;
        }
        if (buffer.length != 2 || buffer[0].contains("-all") || buffer[0].equals("list")) {
            return;
        }

        if (Objects.equals(buffer[0], "reload") || Objects.equals(buffer[0], "disable")) {
            tabList.addAll(ModuleManager.getIdsByStatus(ObjectStatus.ENABLED));
        } else {
            tabList.addAll(ModuleManager.getIdsByStatus(ObjectStatus.DISABLED));
        }
        if (tabList.isEmpty()) {
            tabList.add("(not found)");
        }
    }

    private void listModules(CommandSender sender) {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');

        HashMap<String, List<String>> map = new HashMap<>();
        for (String s : ModuleManager.getAllModules().keySet().stream().sorted().toList()) {
            String namespace = s.split(":")[0];
            if (!map.containsKey(namespace)) {
                map.put(namespace, new ArrayList<>());
            }
            map.get(namespace).add(s.split(":")[1]);
        }

        for (String namespace : map.keySet()) {
            List<String> list = map.get(namespace);
            sb.append(ChatColor.GOLD).append(namespace).append("(").append(list.size()).append("):\n");
            for (String id : list) {
                if (ModuleManager.getModuleStatus(namespace + ":" + id) == ObjectStatus.ENABLED) {
                    sb.append(ChatColor.GREEN);
                } else {
                    sb.append(ChatColor.GRAY);
                }
                AbstractModule module = ModuleManager.getModule(namespace + ":" + id);
                sb.append("   ").append(module.getDisplayName(Language.locale(sender))).append(ChatColor.WHITE).append(" -> ");
                sb.append(module.getVersion());
                sb.append('\n');
            }
        }
        this.getLanguage().sendMessage(sender, "list", sb.toString());
    }


    @Override
    public String getLanguageNamespace() {
        return "module";
    }
}
