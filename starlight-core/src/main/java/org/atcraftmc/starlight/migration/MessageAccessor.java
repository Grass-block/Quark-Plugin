package org.atcraftmc.starlight.migration;

import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.bukkit.command.CommandSender;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.core.LocaleService;

public interface MessageAccessor {
    static void broadcast(LanguageEntry language, boolean b, boolean b1, String s, Object... format) {
        language.item(s).broadcast(b, b1, format);
    }

    static void send(LanguageEntry language, CommandSender sender, String s, Object... format) {
        language.item(s).send(sender, format);
    }

    static String getMessage(LanguageEntry language, MinecraftLocale locale, String s, Object... format) {
        return language.item(s).message(locale, format);
    }

    static void sendTemplate(LanguageEntry language, CommandSender sender, String ui) {
        TextSender.sendMessage(sender, TextBuilder.build(buildTemplate(language, LocaleService.locale(sender), ui)));
    }

    static String buildTemplate(LanguageEntry language, MinecraftLocale locale, String s) {
        return language.inline(s, locale);
    }

}
