package org.atcraftmc.starlight.chat;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.starlight.chat.mail.MailMessage;
import org.atcraftmc.starlight.chat.mail.MailMessageStatus;
import org.atcraftmc.starlight.core.JDBCService;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.migration.QuarkDataImporter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;

@CommandProvider(Mail.MailCommand.class)
public class Mail extends SLPackageModule {


    private final JDBCMailDataService dataService = new JDBCMailDataService();


    @Override
    public void enable() throws Exception {
        this.dataService.init(JDBCService.getDB(JDBCService.SL_SHARED).orElseThrow());
    }

    @QuarkCommand(name = "mail", permission = "+starlight.mail", subCommands = {})
    public static final class MailCommand extends ModuleCommand<Mail> {

    }
}
