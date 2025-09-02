package org.atcraftmc.starlight.internal.command;

import org.atcraftmc.qlib.bukkit.task.TaskScheduler;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.command.CommandSender;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CoreCommand;
import org.atcraftmc.starlight.core.GameTestService;
import org.atcraftmc.starlight.core.TaskService;

@QuarkCommand(name = "debug", permission = "-quark.debug")
public final class DebugCommand extends CoreCommand {
    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "task", "permission","test");
        suggestion.matchArgument(0, "task", (c) -> c.suggest(1, "global", "async", "entity", "region"));
        suggestion.matchArgument(0, "test", (c) -> c.suggest(1, GameTestService.TESTS.keySet()));
    }

    @Override
    public void execute(CommandExecution context) {
        switch (context.requireEnum(0, "task", "permission", "test")) {
            case "task" -> {
                switch (context.requireEnum(1, "global", "async", "entity", "region")) {
                    case "global" -> debugTask(context.getSender(), TaskService.global());
                    case "async" -> debugTask(context.getSender(), TaskService.async());
                }
            }
            case "test" -> GameTestService.run(context.requireEnum(1, GameTestService.TESTS.keySet()));
            case "permission" -> {

            }
        }
    }

    private void debugTask(CommandSender sender, TaskScheduler handle) {
        var name = handle.getClass().getSimpleName();
        var id = handle.hashCode();
        TextSender.sendChatColor(sender, "&aTaskScheduler&f(&b%s&7#&d%s&f)".formatted(name, id));

        for (var tid : handle.tasks()) {
            var task = handle.get(tid);

            TextSender.sendChatColor(sender, "&7" + tid);
        }
    }
}
