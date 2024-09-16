package org.tbstcraft.quark.internal.command;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;

import java.util.List;

@QuarkCommand(name = "globalvars", permission = "-quark.globalvars")
public final class GlobalVarsCommand extends CoreCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "reload" -> {
                PlaceHolderService.reloadExternal();
                Quark.LANGUAGE.sendMessage(sender, "global-var", "reload");
            }
            case "restore" -> {
                PlaceHolderService.EXTERNAL_VARS.restore();
                Quark.LANGUAGE.sendMessage(sender, "global-var", "restore");
            }
            case "sync" -> {
                PlaceHolderService.EXTERNAL_VARS.sync();
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
