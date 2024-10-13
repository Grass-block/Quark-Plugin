package org.tbstcraft.quark.internal.command;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.task.TaskScheduler;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.tbstcraft.quark.foundation.text.TextSender;
import org.tbstcraft.quark.internal.task.TaskService;

@QuarkCommand(name = "debug", permission = "-quark.debug", subCommands = TestCommand.class)
public final class DebugCommand extends CoreCommand {
    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "task", "permission");
        suggestion.matchArgument(0, "task", (c) -> c.suggest(1, "global", "async", "entity", "region"));
    }

    @Override
    public void execute(CommandExecution context) {
        switch (context.requireEnum(0, "task", "permission")) {
            case "task" -> {
                switch (context.requireEnum(1, "global", "async", "entity", "region")) {
                    case "global" -> debugTask(context.getSender(), TaskService.global());
                    case "async" -> debugTask(context.getSender(), TaskService.async());
                }
            }
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
