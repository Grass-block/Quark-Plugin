package org.atcraftmc.quark.web.account;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.foundation.command.CommandExecution;
import org.tbstcraft.quark.foundation.command.CommandSuggestion;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;

@QuarkCommand(name = "account", playerOnly = true)
public final class AccountCommand extends ModuleCommand<AccountActivation> {

    @Override
    public void execute(CommandExecution context) {
        Player p = context.requireSenderAsPlayer();
        CommandSender sender = context.getSender();
        String name = context.getSender().getName();

        context.requireEnum(0, "verify", "link", "unlink", "unverify");

        context.matchArgument(0, "verify", () -> {
            String mail = AccountManager.getMail(name);
            if (!AccountManager.isValidMail(name)) {
                this.getLanguage().sendMessage(sender, "verify-failed");
            }
            String prefix = this.getConfig().getString("verify-link-delegation");
            String code = AccountManager.generateActivationLink(prefix, () -> {
                AccountManager.setStatus(name, AccountStatus.VERIFIED);
                this.getLanguage().sendMessage(sender, "verified");
                this.getModule().unfreeze(p);
            });

            this.getModule().sendVerifyMail(((Player) sender), mail, code);
        });

        context.matchArgument(0, "unverify", () -> {
            AccountManager.setStatus(name, AccountStatus.UNVERIFIED);
            this.getLanguage().sendMessage(sender, "unverify-success");
            this.getModule().freeze(p, AccountStatus.UNVERIFIED);
        });

        context.matchArgument(0, "unlink", () -> {
            String mail = AccountManager.getMail(name);
            if (!AccountManager.isValidMail(name)) {
                this.getLanguage().sendMessage(sender, "unlink-failed");
            }
            String prefix = this.getConfig().getString("verify-link-delegation");
            String code = AccountManager.generateActivationLink(prefix, () -> {
                AccountManager.setMail(name, null);
                this.getLanguage().sendMessage(sender, "unlinked", mail);
                this.getModule().freeze(p, AccountStatus.UNLINKED);
            });

            this.getModule().sendVerifyMail(((Player) sender), mail, code);
        });

        context.matchArgument(0, "link", () -> {
            String prefix = this.getConfig().getString("verify-link-delegation");

            if (AccountManager.isValidMail(name)) {
                this.getLanguage().sendMessage(sender, "link-failed");
                return;
            }

            String mail = context.requireArgumentAt(0);

            String code = AccountManager.generateActivationLink(prefix, () -> {
                AccountManager.setMail(name, mail);
                AccountManager.setStatus(name, AccountStatus.VERIFIED);
                this.getLanguage().sendMessage(sender, "verified");
            });
            this.getModule().sendVerifyMail(((Player) sender), mail, code);
        });
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "link", "unlink", "verify", "unverify");
        suggestion.matchArgument(0, "link", (s) -> s.suggest(1, "@163.com", "@126.com", "@qq.com", "@gmail.com"));
    }
}
