package org.tbstcraft.quark.internal.command;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.data.config.ConfigContainer;
import org.tbstcraft.quark.data.config.Configuration;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;

import java.util.List;

@QuarkCommand(name = "config", permission = "-quark.config")
public final class ConfigCommand extends CoreCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        LanguageEntry language = this.getLanguage();
        switch (args[0]) {
            case "reload-all" -> {
                ConfigContainer.reloadConfigs();
                language.sendMessage(sender, "reload-all");
            }
            case "restore-all" -> {
                ConfigContainer.restoreConfigs();
                language.sendMessage(sender, "restore-all");
            }
            case "restore" -> {
                Configuration file = ConfigContainer.CONFIG_REGISTRY.get(args[1]);
                if (file == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                file.restore();
                language.sendMessage(sender, "restore", args[1]);
            }
            case "reload" -> {
                Configuration file = ConfigContainer.CONFIG_REGISTRY.get(args[1]);
                if (file == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                file.load();
                language.sendMessage(sender, "reload", args[1]);
            }
            case "sync" -> {
                Configuration file = ConfigContainer.getConfig(args[1]);
                if (file == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                file.sync(true);
                language.sendMessage(sender, "sync", args[1]);
            }
            case "sync-all" -> {
                ConfigContainer.syncConfigs(true);
                language.sendMessage(sender, "sync-all");
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
            tabList.addAll(ConfigContainer.CONFIG_REGISTRY.keySet());
        }
    }

    @Override
    public String getLanguageNamespace() {
        return "config";
    }
}
