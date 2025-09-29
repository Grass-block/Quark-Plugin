package org.atcraftmc.quark.web.account;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.migration.MessageAccessor;

@QuarkCommand(name = "account", playerOnly = true)
public final class AccountCommand extends ModuleCommand<AccountActivation> {

    void verify(CommandSender sender, String name, Player player) {
        var mail = AccountManager.getMail(name);
        if (!AccountManager.isValidMail(name)) {
            MessageAccessor.send(this.getLanguage(), sender, "verify-failed-invalid-mail");
        }
        var code = AccountManager.generateActivationLink(() -> {
            AccountManager.setStatus(name, AccountStatus.VERIFIED);
            MessageAccessor.send(this.getLanguage(), sender, "verified");
            this.getModule().unfreeze(player);
        });

        this.getModule().sendVerifyMail(((Player) sender), mail, code);
    }

    void link(CommandSender sender, String name, String mail) {
        if (AccountManager.isValidMail(name)) {
            MessageAccessor.send(this.getLanguage(), sender, "link-failed");
            return;
        }

        var code = AccountManager.generateActivationLink(() -> {
            AccountManager.setMail(name, mail);
            AccountManager.setStatus(name, AccountStatus.VERIFIED);
            getModule().unfreeze((Player) sender);
            MessageAccessor.send(this.getLanguage(), sender, "verified");
        });
        this.getModule().sendVerifyMail(((Player) sender), mail, code);
    }

    void unlink(CommandSender sender, String name, Player player) {
        var mail = AccountManager.getMail(name);
        if (!AccountManager.isValidMail(name)) {
            MessageAccessor.send(this.getLanguage(), sender, "unlink-failed");
        }
        var code = AccountManager.generateActivationLink(() -> {
            AccountManager.setMail(name, null);
            MessageAccessor.send(this.getLanguage(), sender, "unlinked", mail);
            this.getModule().freeze(player, AccountStatus.UNLINKED);
        });

        this.getModule().sendVerifyMail(((Player) sender), mail, code);
    }

    @Override
    public void execute(CommandExecution context) {
        var p = context.requireSenderAsPlayer();
        var sender = context.getSender();
        var name = context.getSender().getName();

        switch (context.requireEnum(0, "verify", "link", "code", "unlink", "unverify")) {
            case "verify" -> this.verify(sender, name, p);
            case "link" -> this.link(sender, name, context.requireArgumentAt(1));
            case "unlink" -> this.unlink(sender, name, p);
            case "unverify" -> {
                AccountManager.setStatus(name, AccountStatus.UNVERIFIED);
                MessageAccessor.send(this.getLanguage(), sender, "unverify-success");
                this.getModule().freeze(p, AccountStatus.UNVERIFIED);
            }
            case "code" -> {
                if (!AccountManager.verifyMail(context.requireArgumentAt(1))) {
                    MessageAccessor.send(this.getLanguage(), sender, "verify-failed");
                }
            }
        }
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "link", "unlink", "verify", "unverify");
        suggestion.matchArgument(0, "link", (s) -> s.suggest(1, "@163.com", "@126.com", "@qq.com", "@gmail.com"));
    }
}
