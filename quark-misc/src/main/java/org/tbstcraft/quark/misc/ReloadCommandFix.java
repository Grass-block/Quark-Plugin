package org.tbstcraft.quark.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.ReloadCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.CommandModule;
import org.tbstcraft.quark.module.QuarkModule;

import java.util.List;

@QuarkModule
@QuarkCommand(name = "reload", op = true)
public final class ReloadCommandFix extends CommandModule {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
        tabList.addAll(List.of("confirm", "permissions", "commands"));
    }

    @Override
    public Command getCoveredCommand() {
        return new ReloadCommand("reload");
    }
}
