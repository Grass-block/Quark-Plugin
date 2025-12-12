package org.atcraftmc.starlight.utilities;

import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.assertion.NumberLimitation;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.SLCommandModule;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
@ApplicationModule(id = "tickAppend-manager")
@QuarkCommand(name = "server-tickAppend", permission = "-quark.tickAppend")
public final class TickManager extends SLCommandModule {

    @Inject
    private LanguageEntry language;

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireClass(() -> Class.forName("org.bukkit.ServerTickManager"));
    }

    @Override
    public void execute(CommandExecution execution) {
        switch (execution.requireEnum(0, "freeze", "unfreeze", "step")) {
            case "freeze" -> {
                Bukkit.getServer().getServerTickManager().setFrozen(true);
                MessageAccessor.send(this.language, execution.getSender(), "freeze");
            }
            case "unfreeze" -> {
                Bukkit.getServer().getServerTickManager().setFrozen(false);
                MessageAccessor.send(this.language, execution.getSender(), "unfreeze");
            }
            case "step" -> {
                var stp = execution.requireArgumentInteger(1, NumberLimitation.any());
                Bukkit.getServer().getServerTickManager().stepGameIfFrozen(stp);
                MessageAccessor.send(this.language, execution.getSender(), "step", stp);
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
