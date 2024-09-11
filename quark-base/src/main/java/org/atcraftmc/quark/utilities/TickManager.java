package org.atcraftmc.quark.utilities;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.command.execute.CommandExecution;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.command.assertion.NumberLimitation;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.util.List;
import java.util.Objects;

@QuarkModule(version = "1.0")
@QuarkCommand(name = "server-tick", permission = "-quark.tick")
public final class TickManager extends CommandModule {

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireClass(() -> Class.forName("org.bukkit.ServerTickManager"));
    }

    @Override
    public void execute(CommandExecution execution) {
        switch (execution.requireEnum(0, "freeze", "unfreeze", "step")) {
            case "freeze" -> {
                Bukkit.getServer().getServerTickManager().setFrozen(true);
                getLanguage().sendMessage(execution.getSender(), "freeze");
            }
            case "unfreeze" -> {
                Bukkit.getServer().getServerTickManager().setFrozen(false);
                getLanguage().sendMessage(execution.getSender(), "unfreeze");
            }
            case "step" -> {
                var stp = execution.requireArgumentInteger(1, NumberLimitation.any());
                Bukkit.getServer().getServerTickManager().stepGameIfFrozen(stp);
                getLanguage().sendMessage(execution.getSender(), "step", stp);
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("freeze");
            tabList.add("unfreeze");
            tabList.add("step");
        }
        if (buffer.length == 2 && Objects.equals(buffer[0], "step")) {
            tabList.addAll(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
        }
    }
}
