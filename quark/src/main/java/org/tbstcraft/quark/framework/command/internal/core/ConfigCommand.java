package org.tbstcraft.quark.framework.command.internal.core;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.framework.command.CoreCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.config.ConfigDelegation;
import org.tbstcraft.quark.framework.config.Configuration;
import org.tbstcraft.quark.framework.config.LanguageEntry;

import java.util.List;

@QuarkCommand(name = "config", permission = "-quark.configure.config")
public final class ConfigCommand extends CoreCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        LanguageEntry language = this.getLanguage();
        switch (args[0]) {
            case "reload-all" -> {
                ConfigDelegation.reloadConfigs();
                language.sendMessageTo(sender, "reload-all");
            }
            case "restore-all" -> {
                ConfigDelegation.restoreConfigs();
                language.sendMessageTo(sender, "restore-all");
            }
            case "restore" -> {
                Configuration file = ConfigDelegation.CONFIG_REGISTRY.get(args[1]);
                if (file == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                file.restore();
                language.sendMessageTo(sender, "restore", args[1]);
            }
            case "reload" -> {
                Configuration file = ConfigDelegation.CONFIG_REGISTRY.get(args[1]);
                if (file == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                file.load();
                language.sendMessageTo(sender, "reload", args[1]);
            }
            case "sync" -> {
                Configuration file = ConfigDelegation.getConfig(args[1]);
                if (file == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                file.sync(true);
                language.sendMessageTo(sender, "sync", args[1]);
            }
            case "sync-all" -> {
                ConfigDelegation.syncConfigs(true);
                language.sendMessageTo(sender, "sync-all");
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("reload");
            tabList.add("restore");
            tabList.add("reload-all");
            tabList.add("restore-all");
            tabList.add("sync");
            tabList.add("sync-all");
        }
        if (buffer.length == 2 && !buffer[0].contains("-all")) {
            tabList.addAll(ConfigDelegation.CONFIG_REGISTRY.keySet());
        }
    }

    @Override
    public String getLanguageNamespace() {
        return "config";
    }
}
