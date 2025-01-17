package org.atcraftmc.quark.chat;

import org.bukkit.command.CommandSender;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

@QuarkModule
@QuarkCommand(name = "self-msg")
public final class SelfMessage extends CommandModule {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String s : args) {
            sb.append(s).append(" ");
        }
        TextBuilder.build(sb.toString()).send(sender);
    }
}
