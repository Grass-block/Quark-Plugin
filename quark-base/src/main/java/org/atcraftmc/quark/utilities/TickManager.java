package org.atcraftmc.quark.utilities;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.assertion.NumberLimitation;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;

import java.util.List;
import java.util.Objects;

@SLModule(version = "1.0")
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
                MessageAccessor.send(this.getLanguage(), execution.getSender(), "freeze");
            }
            case "unfreeze" -> {
                Bukkit.getServer().getServerTickManager().setFrozen(false);
                MessageAccessor.send(this.getLanguage(), execution.getSender(), "unfreeze");
            }
            case "step" -> {
                var stp = execution.requireArgumentInteger(1, NumberLimitation.any());
                Bukkit.getServer().getServerTickManager().stepGameIfFrozen(stp);
                MessageAccessor.send(this.getLanguage(), execution.getSender(), "step", stp);
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
