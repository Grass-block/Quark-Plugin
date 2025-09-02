package org.atcraftmc.starlight.internal.command;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.command.CommandSender;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.foundation.command.CoreCommand;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;

import java.util.List;

@QuarkCommand(name = "globalvars", permission = "-quark.globalvars")
public final class GlobalVarsCommand extends CoreCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "reload" -> {
                PlaceHolderService.reloadExternal();
                Starlight.LANGUAGE.item("global-var:reload").send(sender);
            }
            case "restore" -> {
                PlaceHolderService.EXTERNAL_VARS.restore();
                Starlight.LANGUAGE.item("global-var:restore").send(sender);
            }
            case "sync" -> {
                PlaceHolderService.EXTERNAL_VARS.sync();
                Starlight.LANGUAGE.item("global-var:sync").send(sender);
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
