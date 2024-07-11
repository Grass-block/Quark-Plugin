package org.tbstcraft.quark.internal.command;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.ProductInfo;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.command.CommandManager;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
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
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "info" -> ProductInfo.sendInfoDisplay(sender);
            case "stats" -> ProductInfo.sendStatsDisplay(sender);
            case "sync-commands" -> {
                CommandManager.sync();
                Quark.LANGUAGE.sendMessage(sender, "command", "sync-commands");
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

    @QuarkCommand(name = "reload",permission = "-quark.reload")
    public static final class ReloadCommand extends CoreCommand {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (APIProfileTest.isArclightBasedServer()) {
                //this.getLanguage().sendMessage(sender, "platform-unsupported");
                //return;
            }
            if (Quark.PLUGIN.isFastBoot()) {
                this.getLanguage().sendMessage(sender, "fastboot-unsupported");
                return;
            }

            Quark.reload(sender);
        }
    }
}
