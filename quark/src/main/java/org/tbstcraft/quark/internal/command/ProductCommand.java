package org.tbstcraft.quark.internal.command;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.framework.command.CoreCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.data.language.LanguageEntry;
import org.tbstcraft.quark.service.base.ProductService;

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
