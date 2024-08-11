package org.tbstcraft.quark.utilities;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.tbstcraft.quark.foundation.command.CommandManager;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.util.FilePath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@QuarkModule(version = "1.2.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class CommandTabFix extends PackageModule {
    @Override
    public void enable() {
        TaskService.laterTask(1000, CommandManager::sync);
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

        if (event.getBuffer().startsWith("reload") || event.getBuffer().startsWith("/reload")) {
            if (!event.getCompletions().contains("confirm")) {
                List<String> list = new ArrayList<>(event.getCompletions());
                list.add("confirm");
                event.setCompletions(list);
            }
        }

        if (event.getBuffer().startsWith("/schem") || event.getBuffer().startsWith("//schem")) {
            if(Objects.equals(lastArg, "load") || Objects.equals(lastArg, "delete")){
                List<String> list = new ArrayList<>(event.getCompletions());

                File folder= new File(FilePath.pluginsFolder() + "/WorldEdit/schematics");

                for (File f: Objects.requireNonNull(folder.listFiles())){
                    list.add(f.getName());
                }

                event.setCompletions(list);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        CommandManager.sync();
    }
}
