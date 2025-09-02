package org.atcraftmc.quark.commands;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;

@SLModule
@QuarkCommand(name = "self-msg")
public final class SelfMessageCommand extends CommandModule {
    @Override
    public void execute(CommandExecution context) {
        TextSender.sendMessage(context.getSender(), TextBuilder.build(context.requireRemainAsParagraph(0, true)));
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "[messages...]");
    }
}
