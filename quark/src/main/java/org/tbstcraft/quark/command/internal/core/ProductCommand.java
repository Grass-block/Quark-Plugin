package org.tbstcraft.quark.command.internal.core;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.command.CoreCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.framework.config.LanguageEntry;
import org.tbstcraft.quark.service.base.ProductService;

@QuarkCommand(name = "product")
public class ProductCommand extends CoreCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        LanguageEntry language = this.getLanguage();

        switch (args[0]) {
            case "system-id" -> language.sendMessageTo(sender, "sys_id", ProductService.getSystemIdentifier());
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
