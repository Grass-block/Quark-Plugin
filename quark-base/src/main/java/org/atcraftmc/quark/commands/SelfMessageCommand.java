package org.atcraftmc.quark.commands;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.framework.module.SLCommandModule;
import me.gb2022.modular.module.ApplicationModule;

@ApplicationModule
@QuarkCommand(name = "self-msg")
public final class SelfMessageCommand extends SLCommandModule {
    @Override
    public void execute(CommandExecution context) {
        TextSender.sendMessage(context.getSender(), TextBuilder.build(context.requireRemainAsParagraph(0, true)));
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "[messages...]");
    }
}
