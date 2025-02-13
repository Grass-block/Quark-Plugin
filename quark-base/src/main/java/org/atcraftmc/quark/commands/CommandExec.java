package org.atcraftmc.quark.commands;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.command.select.EntitySelector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

@QuarkModule
@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkCommand(name = "exec", permission = "-quark.util.exec")
public final class CommandExec extends CommandModule {
    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireMethod(() -> Location.class.getDeclaredMethod("getNearbyEntities", double.class, double.class, double.class));
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        EntitySelector.tab(suggestion, 0);

        if (suggestion.getBuffer().size() > 1) {
            var name = suggestion.getBuffer().get(1);
            var cmd = LegacyCommandManager.getCommandMap().getCommand(name);

            if (cmd == null) {
                return;
            }


            var origin = suggestion.getBuffer().toArray(new String[0]);
            var subArgs = new String[origin.length - 2];

            System.arraycopy(origin, 2, subArgs, 0, origin.length - 2);

            suggestion.suggest(suggestion.getBuffer().size() - 1, cmd.tabComplete(suggestion.getSender(), name, subArgs));
        }
    }

    @Override
    public void execute(CommandExecution context) {
        var target = EntitySelector.selectEntity(context, 0);
        var cmd = context.requireRemainAsParagraph(1, true);

        for (var p : target) {
            Bukkit.dispatchCommand(p, cmd);
        }
        getLanguage().sendMessage(context.getSender(), "hint", target.size(), "/" + cmd);
    }
}
