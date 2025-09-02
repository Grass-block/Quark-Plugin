package org.atcraftmc.starlight.internal.command;

import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.starlight.ProductInfo;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CoreCommand;
import org.atcraftmc.starlight.foundation.platform.APIProfileTest;
import org.bukkit.command.CommandSender;

import java.util.List;

@QuarkCommand(name = "starlight", permission = "+starlight.command.core", subCommands = {
        ConfigCommand.class,
        LanguageCommand.class,
        ModuleCommand.class,
        GlobalVarsCommand.class,
        PackageCommand.class,
        StarlightPluginCommand.ReloadCommand.class,
        DebugCommand.class
})
public final class StarlightPluginCommand extends CoreCommand {

    @Override
    public void execute(CommandExecution context) {
        switch (context.requireEnum(0, "info", "stats", "sync-commands")) {
            case "info" -> ProductInfo.sendInfoDisplay(context.getSender());
            case "stats" -> ProductInfo.sendStatsDisplay(context.getSender());
            case "sync-commands" -> {
                LegacyCommandManager.sync();
                Starlight.LANGUAGE.item("command:sync-commands").send(context.getSender());
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("info");
            tabList.add("sync-commands");
            tabList.add("stats");
        }
    }


    @Override
    public void suggest(CommandSuggestion suggestion) {
        super.suggest(suggestion);
    }

    @QuarkCommand(name = "reload", permission = "-starlight.reload")
    public static final class ReloadCommand extends CoreCommand {

        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, "prepare", "action");
        }

        @Override
        public void execute(CommandExecution context) {
            if (!context.hasArgumentAt(0)) {
                this.getLanguage().item("reload-logic-updated").send(context.getSender());
            }

            switch (context.requireEnum(0, "prepare", "action")) {
                case "prepare" -> {
                    var loc = LocaleService.locale(context.getSender());
                    Starlight.instance().onDisable();
                    InternalCommands.register();

                    TextSender.sendMessage(context.getSender(), this.getLanguage().item("prepared").component(loc));
                }
                case "action" -> {
                    if (APIProfileTest.isMixedServer()) {
                        this.getLanguage().item("platform-unsupported").send(context.getSender());
                        return;
                    }
                    if (Starlight.instance().isFastBoot()) {
                        this.getLanguage().item("fastboot-unsupported").send(context.getSender());
                        return;
                    }

                    Starlight.reload(context.getSender());
                }
            }
        }
    }
}
