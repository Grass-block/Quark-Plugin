package org.tbstcraft.quark.utilities;

import org.bukkit.event.EventHandler;
import org.bukkit.event.server.TabCompleteEvent;
import org.tbstcraft.quark.framework.command.CommandManager;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.service.task.TaskService;

import java.util.ArrayList;
import java.util.List;

@QuarkModule(version = "1.2.0")
@EventListener
public final class CommandTabFix extends PackageModule {
    @Override
    public void enable() {
        TaskService.laterTask(1000, CommandManager::syncCommands);
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        List<String> match = new ArrayList<>();

        String[] args = event.getBuffer().split(" ");
        if (args.length <= 1) {
            return;
        }
        String lastArg = args[args.length - 1];

        if (event.getBuffer().charAt(event.getBuffer().length() - 1) != ' ') {
            for (String s : event.getCompletions()) {
                if (!s.contains(lastArg)) {
                    continue;
                }
                match.add(s);
            }
            event.setCompletions(match);
        }
        /*
        if (!event.getBuffer().startsWith("reload") && !event.getBuffer().startsWith("/reload")) {
            return;
        }
        if (event.getCompletions().contains("confirm")) {
            return;
        }
        List<String> list = new ArrayList<>(event.getCompletions());
        list.add("confirm");
        event.setCompletions(list);
         */
    }
}
