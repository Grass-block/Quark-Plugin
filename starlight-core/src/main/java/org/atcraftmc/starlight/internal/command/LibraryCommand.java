package org.atcraftmc.starlight.internal.command;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.platform.PluginPlatform;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CoreCommand;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Date;

@QuarkCommand(name = "libraries", permission = "-starlight.core.libs")
public final class LibraryCommand extends CoreCommand {

    @Override
    public void execute(CommandExecution context) {
        list(context);
    }

    public void list(CommandExecution context) {
        var paths = Starlight.instance().getLibraryManager().getLoadedURLs();
        var sender = context.requireSenderAsPlayer();
        var locale = LocaleService.locale(sender);
        var list = this.getLanguage().item("list").component(locale);

        paths.forEach((id, lib) -> {
            File file;
            try {
                file = new File(lib.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            var component = getLanguage().item("list-item").component(locale, id);
            var hover = getLanguage().item("list-hover").component(
                    locale,
                    id,
                    file.getName(),
                    file.length() / 1024,
                    SharedObjects.DATE_FORMAT.format(new Date(file.lastModified())),
                    file.getAbsolutePath()
            );

            var line = component.asComponent().hoverEvent(hover.asComponent().asHoverEvent());
            list.add(line);
        });

        sender.sendMessage(PluginPlatform.global().globalFormatMessage("{#line}"));
        TextSender.sendMessage(sender, list);
        sender.sendMessage(PluginPlatform.global().globalFormatMessage("{#line}"));
    }
}
