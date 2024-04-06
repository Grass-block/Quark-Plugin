package org.tbstcraft.quark.command.internal.core;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.command.CoreCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.config.ConfigDelegation;
import org.tbstcraft.quark.config.Language;

import java.util.List;

@QuarkCommand(name = "language", permission = "-quark.configure.language")
public final class LanguageCommand extends CoreCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "reload-all" -> {
                ConfigDelegation.reloadLanguages();
                Quark.LANGUAGE.sendMessageTo(sender, "language", "reload-all");
            }
            case "restore-all" -> {
                ConfigDelegation.restoreLanguages();
                Quark.LANGUAGE.sendMessageTo(sender, "language", "restore-all");
            }
            case "restore" -> {
                Language file = ConfigDelegation.getLanguage(args[1]);
                if (file == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                file.restore();
                Quark.LANGUAGE.sendMessageTo(sender, "language", "restore", args[1]);
            }
            case "reload" -> {
                Language file = ConfigDelegation.getLanguage(args[1]);
                if (file == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                file.reload();
                Quark.LANGUAGE.sendMessageTo(sender, "language", "reload", args[1]);
            }
            case "sync" -> {
                Language file = ConfigDelegation.getLanguage(args[1]);
                if (file == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                file.sync(true);
                Quark.LANGUAGE.sendMessageTo(sender, "language", "sync", args[1]);
            }
            case "sync-all" -> {
                ConfigDelegation.syncLanguages(true);
                Quark.LANGUAGE.sendMessageTo(sender, "language", "sync-all");
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
