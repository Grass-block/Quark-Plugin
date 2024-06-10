package org.tbstcraft.quark.internal.command;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.command.CoreCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.data.config.ConfigDelegation;
import org.tbstcraft.quark.framework.data.config.Language;
import org.tbstcraft.quark.framework.data.language.LanguageEntry;

import java.util.List;

@QuarkCommand(name = "language", permission = "-quark.configure.language")
public final class LanguageCommand extends CoreCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        LanguageEntry entry = Quark.LANGUAGE.entry("language");

        switch (args[0]) {
            case "reload-all" -> {
                ConfigDelegation.reloadLanguages();
                entry.sendMessage(sender, "reload-all");
            }
            case "restore-all" -> {
                ConfigDelegation.restoreLanguages();
                entry.sendMessage(sender, "restore-all");
            }
            case "restore" -> {
                Language file = ConfigDelegation.getLanguage(args[1]);
                if (file == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                file.restore();
                entry.sendMessage(sender, "restore", args[1]);
            }
            case "reload" -> {
                Language file = ConfigDelegation.getLanguage(args[1]);
                if (file == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                file.reload();
                entry.sendMessage(sender, "reload", args[1]);
            }
            case "sync" -> {
                Language file = ConfigDelegation.getLanguage(args[1]);
                if (file == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                file.sync(true);
                entry.sendMessage(sender, "sync", args[1]);
            }
            case "sync-all" -> {
                ConfigDelegation.syncLanguages(true);
                entry.sendMessage(sender, "sync-all");
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
            return;
        }
        if (buffer.length == 2 && !buffer[0].contains("-all")) {
            tabList.addAll(ConfigDelegation.LANGUAGE_REGISTRY.keySet());
        }
    }
}
