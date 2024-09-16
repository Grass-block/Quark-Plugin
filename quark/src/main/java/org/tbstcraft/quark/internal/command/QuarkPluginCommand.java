package org.tbstcraft.quark.internal.command;

import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.ProductInfo;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;

import java.util.List;

@QuarkCommand(name = "quark", permission = "+quark.command", subCommands = {
        ConfigCommand.class,
        LanguageCommand.class,
        ModuleCommand.class,
        GlobalVarsCommand.class,
        PackageCommand.class,
        QuarkPluginCommand.ReloadCommand.class
})
public final class QuarkPluginCommand extends CoreCommand {
    @Override
    public void execute(CommandExecution context) {
        switch (context.requireEnum(0, "info", "stats", "sync-commands")) {
            case "info" -> ProductInfo.sendInfoDisplay(context.getSender());
            case "stats" -> ProductInfo.sendStatsDisplay(context.getSender());
            case "sync-commands" -> {
                LegacyCommandManager.sync();
                Quark.LANGUAGE.sendMessage(context.getSender(), "command", "sync-commands");
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

    @QuarkCommand(name = "reload", permission = "-quark.reload")
    public static final class ReloadCommand extends CoreCommand {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (APIProfileTest.isArclightBasedServer()) {
                this.getLanguage().sendMessage(sender, "platform-unsupported");
                return;
            }
            if (Quark.getInstance().isFastBoot()) {
                this.getLanguage().sendMessage(sender, "fastboot-unsupported");
                return;
            }

            Quark.reload(sender);
        }
    }
}
