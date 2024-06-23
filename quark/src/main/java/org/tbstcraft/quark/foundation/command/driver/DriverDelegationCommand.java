package org.tbstcraft.quark.foundation.command.driver;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.tbstcraft.quark.foundation.command.AbstractCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;

import java.util.List;

@QuarkCommand(name = ".")
public final class DriverDelegationCommand extends AbstractCommand {
    private final String name;

    public DriverDelegationCommand(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {

    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

    }
}
