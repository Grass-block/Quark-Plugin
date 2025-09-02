package org.atcraftmc.starlight.internal.command;

import org.bukkit.command.CommandSender;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.foundation.command.CoreCommand;
import org.atcraftmc.qlib.command.QuarkCommand;

@QuarkCommand(name = "product")
public final class ProductCommand extends CoreCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        LanguageEntry language = this.getLanguage();

        switch (args[0]) {

        }
    }

    @Override
    public String getLanguageNamespace() {
        return "activation";
    }
}
