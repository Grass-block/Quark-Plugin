package org.tbstcraft.quark.internal.command;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.data.config.Queries;

import java.util.List;

@QuarkCommand(name = "global-vars", permission = "-quark.global-vars")
public final class GlobalVarsCommand extends CoreCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "reload" -> {
                Queries.EXTERNAL_VARS.load();
                Quark.LANGUAGE.sendMessage(sender, "global-var", "reload");
            }
            case "restore" -> {
                Queries.EXTERNAL_VARS.restore();
                Quark.LANGUAGE.sendMessage(sender, "global-var", "restore");
            }
            case "sync" -> {
                Queries.EXTERNAL_VARS.sync();
                Quark.LANGUAGE.sendMessage(sender, "global-var", "sync");
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("reload");
            tabList.add("restore");
            tabList.add("sync");
        }
    }
}
