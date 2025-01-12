package org.tbstcraft.quark.internal.command;

import org.bukkit.command.CommandSender;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.internal.ProductService;

@QuarkCommand(name = "product")
public final class ProductCommand extends CoreCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        LanguageEntry language = this.getLanguage();

        switch (args[0]) {
            case "system-id" -> language.sendMessage(sender, "sys-id", ProductService.getSystemIdentifier());
            case "activate" -> {
            }
            case "check-update" -> {
            }
        }
    }

    @Override
    public String getLanguageNamespace() {
        return "activation";
    }
}
