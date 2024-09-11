package org.atcraftmc.quark.chat;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.text.TextBuilder;
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
