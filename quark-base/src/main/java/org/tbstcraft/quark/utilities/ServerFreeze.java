package org.tbstcraft.quark.utilities;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.service.task.TaskService;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.util.List;
import java.util.Objects;

@QuarkModule(version = "0.3", beta = true)
@QuarkCommand(name = "freeze", op = true)
public class ServerFreeze extends CommandModule implements Runnable {
    public static final String TASK_ID = "freeze::lock";

    @Override
    public void run() {
        long a = 0;
        while (a < Long.MAX_VALUE) {
            Thread.yield();
            a++;
        }
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "freeze" -> TaskService.timerTask(TASK_ID, 0, 1, this);
            case "unfreeze" -> TaskService.cancelTask(TASK_ID);
            case "step" -> {
                TaskService.cancelTask(TASK_ID);
                TaskService.timerTask(TASK_ID, Long.parseLong(args[1]), 1, this);
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
