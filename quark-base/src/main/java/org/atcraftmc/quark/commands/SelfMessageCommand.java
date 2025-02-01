package org.atcraftmc.quark.commands;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

@QuarkModule
@QuarkCommand(name = "self-msg")
public final class SelfMessageCommand extends CommandModule {
    @Override
    public void execute(CommandExecution context) {
        TextBuilder.build(context.requireRemainAsParagraph(-1, true))
                .send(context.getSender());
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "[messages...]");
    }
}
