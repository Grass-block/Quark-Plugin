package org.tbstcraft.quark.internal.command;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.language.LanguageContainer;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.data.language.LanguagePack;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;

import java.util.List;

@QuarkCommand(name = "language", permission = "-quark.language")
public final class LanguageCommand extends CoreCommand {
    private final LanguageContainer container = LanguageContainer.INSTANCE;

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        LanguageEntry entry = Quark.LANGUAGE.entry("language");

        switch (args[0]) {
            case "reload-all" -> {
                for (LanguagePack pack : this.container.getPacks()) {
                    pack.load();
                }
                LanguageContainer.INSTANCE.refresh(true);
                entry.sendMessage(sender, "reload-all");
            }
            case "restore-all" -> {
                for (LanguagePack pack : this.container.getPacks()) {
                    pack.restore();
                }
                LanguageContainer.INSTANCE.refresh(true);
                entry.sendMessage(sender, "restore-all");
            }
            case "restore" -> {
                LanguagePack pack = LanguageContainer.INSTANCE.getPack(args[1]);
                if (pack == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                pack.restore();
                entry.sendMessage(sender, "restore", args[1]);
            }
            case "reload" -> {
                LanguagePack pack = LanguageContainer.INSTANCE.getPack(args[1]);
                if (pack == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                pack.load();
                entry.sendMessage(sender, "reload", args[1]);
            }
            case "sync" -> {
                LanguagePack pack = LanguageContainer.INSTANCE.getPack(args[1]);
                if (pack == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                pack.sync(true);
                entry.sendMessage(sender, "sync", args[1]);
            }
            case "sync-all" -> {
                for (LanguagePack pack : this.container.getPacks()) {
                    pack.sync(true);
                }
                LanguageContainer.INSTANCE.refresh(true);
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
            //tabList.add("restore-all");
            tabList.add("sync");
            tabList.add("sync-all");
            return;
        }
        if (buffer.length == 2 && !buffer[0].contains("-all")) {
            tabList.addAll(this.container.getPackStorage().keySet());
        }
    }
}
