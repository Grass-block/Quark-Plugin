package org.atcraftmc.starlight.internal.command;

import net.kyori.adventure.text.event.ClickEvent;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.platform.PluginPlatform;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CoreCommand;

import java.sql.SQLException;

public class LibraryCommand extends CoreCommand {
    public void list(CommandExecution context) {
        var sender = context.requireSenderAsPlayer();
        var locale = LocaleService.locale(sender);
        var list = this.getLanguage().item("list-libraries").component(locale);

        var path

        try {
            this.service.listAccessible(sender.getUniqueId()).stream().filter((w) -> !w.getName().endsWith("#home")).forEach((w) -> {
                var name = w.getName();
                var owner = w.getOwner();
                var uuid = w.getUuid();
                var component = getLanguage().item("list-item")
                        .component(locale, name, w.getWorld(), ((int) w.getX()), ((int) w.getY()), ((int) w.getZ()), name);
                var hover = getLanguage().item("list-hover")
                        .component(locale, name, w.getWorld(), w.getX(), w.getY(), w.getZ(), w.getYaw(), w.getPitch(), owner, uuid);
                var line = component.asComponent()
                        .hoverEvent(hover.asComponent().asHoverEvent())
                        .clickEvent(ClickEvent.runCommand("/waypoint tp %s".formatted(name)));

                list.add(line);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        sender.sendMessage(PluginPlatform.global().globalFormatMessage("{#line}"));
        TextSender.sendMessage(sender, list);
        sender.sendMessage(PluginPlatform.global().globalFormatMessage("{#line}"));
    }
}
