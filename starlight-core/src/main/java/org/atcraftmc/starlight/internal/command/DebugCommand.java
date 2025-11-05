package org.atcraftmc.starlight.internal.command;

import org.atcraftmc.qlib.bukkit.task.TaskScheduler;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.starlight.core.GameTestService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CoreCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Objects;

@QuarkCommand(name = "debug", permission = "-quark.debug")
public final class DebugCommand extends CoreCommand {
    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "task", "permission", "test");
        suggestion.matchArgument(0, "task", (c) -> c.suggest(1, "global", "async", "entity", "region"));
        suggestion.matchArgument(0, "test", (c) -> c.suggest(1, GameTestService.TESTS.keySet()));
    }

    @Override
    public void execute(CommandExecution context) {
        switch (context.requireEnum(0, "task", "permission", "test", "dev1")) {
            case "task" -> {
                switch (context.requireEnum(1, "global", "async", "entity", "region")) {
                    case "global" -> debugTask(context.getSender(), TaskService.global());
                    case "async" -> debugTask(context.getSender(), TaskService.async());
                }
            }
            case "test" -> GameTestService.run(context.requireEnum(1, GameTestService.TESTS.keySet()));
            case "permission" -> {

            }
            case "dev1" -> {
                var cmd = context.requireRemainAsParagraph(1, true);
                var actor = (context.getSender());

                for (var l : cmd.split(";")) {
                    var a = l.split(" ");

                    if (Objects.equals(a[0], "f")) {
                        Bukkit.dispatchCommand(actor, "fill %s %s %s %s %s %s %s".formatted(a[1], a[2], a[3], a[4], a[5], a[6], a[7]));
                        continue;
                    }
                    Bukkit.dispatchCommand(actor, "setblock %s %s %s %s".formatted(a[1], a[2], a[3], a[4]));
                }
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
