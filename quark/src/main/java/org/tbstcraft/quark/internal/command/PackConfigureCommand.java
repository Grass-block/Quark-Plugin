package org.tbstcraft.quark.internal.command;

import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.command.CommandSender;
import org.atcraftmc.qlib.config.ConfigurationPack;
import org.atcraftmc.qlib.config.PackContainer;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.tbstcraft.quark.foundation.command.CoreCommand;

import java.util.List;
import java.util.function.Consumer;

public abstract class PackConfigureCommand extends CoreCommand {
    private final PackContainer<?> container = getPackContainer();

    public abstract LanguageEntry getLanguageEntry();

    public abstract PackContainer<?> getPackContainer();

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "reload", "reload-all", "restore-all", "restore", "sync", "sync-all");
        Consumer<CommandSuggestion> func = (ctx) -> ctx.suggest(1, this.container.getPackStorage().keySet());

        suggestion.matchArgument(0, "reload", func);
        suggestion.matchArgument(0, "restore", func);
        suggestion.matchArgument(0, "sync", func);
    }

    @Override
    public void execute(CommandExecution context) {
        LanguageEntry entry = getLanguageEntry();

        var sender = context.getSender();

        switch (context.requireEnum(0, "reload-all", "restore-all", "sync-all", "reload", "restore", "sync")) {
            case "reload-all" -> {
                for (ConfigurationPack pack : this.container.getPacks()) {
                    pack.load();
                }
                this.container.refresh(true);
                entry.sendMessage(sender, "reload-all");
            }
            case "restore-all" -> {
                for (ConfigurationPack pack : this.container.getPacks()) {
                    pack.restore();
                }
                this.container.refresh(true);
                entry.sendMessage(sender, "restore-all");
            }
            case "restore" -> {
                var name = context.requireArgumentAt(1);

                ConfigurationPack pack = this.container.getPack(name);
                if (pack == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                pack.restore();
                entry.sendMessage(sender, "restore", name);
            }
            case "reload" -> {
                var name = context.requireArgumentAt(1);

                ConfigurationPack pack = this.container.getPack(name);
                if (pack == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                pack.load();
                entry.sendMessage(sender, "reload", name);
            }
            case "sync" -> {
                var name = context.requireArgumentAt(1);

                ConfigurationPack pack = this.container.getPack(name);
                if (pack == null) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                pack.sync(true);
                entry.sendMessage(sender, "sync", name);
            }
            case "sync-all" -> {
                for (ConfigurationPack pack : this.container.getPacks()) {
                    pack.sync(true);
                }
                this.container.refresh(true);
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
