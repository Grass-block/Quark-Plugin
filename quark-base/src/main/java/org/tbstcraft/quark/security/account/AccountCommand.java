package org.tbstcraft.quark.security.account;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;

import java.util.List;
import java.util.Objects;

@QuarkCommand(name = "account", subCommands = AccountCommand.LinkCommand.class, playerOnly = true)
public final class AccountCommand extends ModuleCommand<AccountActivation> {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player p = ((Player) sender);


        switch (args[0]) {
            case "verify" -> {
                String mail = AccountManager.getMail(sender.getName());
                if (!AccountManager.isValidMail(sender.getName())) {
                    this.getLanguage().sendMessage(sender, "verify-failed");
                }
                String prefix = this.getConfig().getString("verify-link-delegation");
                String code = AccountManager.generateActivationLink(prefix, () -> {
                    AccountManager.setStatus(sender.getName(), AccountStatus.VERIFIED);
                    this.getLanguage().sendMessage(sender, "verified");
                    this.getModule().unfreeze(p);
                });

                this.getModule().sendVerifyMail(((Player) sender), mail, code);
            }
            case "unverify" -> {
                String player = sender.getName();
                AccountManager.setStatus(player, AccountStatus.UNVERIFIED);
                this.getLanguage().sendMessage(sender, "unverify-success");
                this.getModule().freeze(p, AccountStatus.UNVERIFIED);
            }
            case "unlink" -> {
                String mail = AccountManager.getMail(sender.getName());
                if (!AccountManager.isValidMail(sender.getName())) {
                    this.getLanguage().sendMessage(sender, "unlink-failed");
                }
                String prefix = this.getConfig().getString("verify-link-delegation");
                String code = AccountManager.generateActivationLink(prefix, () -> {
                    AccountManager.setMail(sender.getName(), null);
                    this.getLanguage().sendMessage(sender, "unlinked", mail);
                    this.getModule().freeze(p, AccountStatus.UNLINKED);
                });

                this.getModule().sendVerifyMail(((Player) sender), mail, code);
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("link");
            tabList.add("verify");
            tabList.add("unverify");
            tabList.add("unlink");
        }
        if (buffer.length == 2 && Objects.equals(buffer[0], "link")) {
            tabList.add("example@example.com");
        }
    }

    @QuarkCommand(name = "link", playerOnly = true)
    public static final class LinkCommand extends ModuleCommand<AccountActivation> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            String prefix = this.getConfig().getString("verify-link-delegation");

            if (AccountManager.isValidMail(sender.getName())) {
                this.getLanguage().sendMessage(sender, "link-failed");
                return;
            }
            String code = AccountManager.generateActivationLink(prefix, () -> {
                AccountManager.setMail(sender.getName(), args[0]);
                AccountManager.setStatus(sender.getName(), AccountStatus.VERIFIED);
                this.getLanguage().sendMessage(sender, "verified");
            });
            this.getModule().sendVerifyMail(((Player) sender), args[0], code);
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("example@example.com");
                tabList.add("@163.com");
                tabList.add("@126.com");
                tabList.add("@qq.com");
                tabList.add("@gmail.com");
            }
        }
    }
}
