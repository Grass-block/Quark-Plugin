package org.tbstcraft.quark.foundation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.language.LanguageContainer;
import org.atcraftmc.qlib.texts.TextBuilder;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public interface TextExaminer {
    static Component examinable(String id, Object... format) {
        return Component.text(examinableText(id, format));
    }

    static String examinableText(String id, Object... format) {
        return "{msg#%s=%s}".formatted(id, String.join(";;", Arrays.stream(format).map(Object::toString).collect(Collectors.toSet())));
    }

    static Component searchResult(Locale loc, String string, boolean random) {
        var data = string.substring(random ? 6 : 5, string.length() - 1);

        var sp = data.split("=");

        var id = sp[0].split(":");
        var param = sp.length > 1 ? sp[1].split(";;") : new Object[0];

        if (!random) {
            return TextBuilder.buildComponent(LanguageContainer.getInstance().getMessage(loc, id[0], id[1], id[2], param));
        }

        return TextBuilder.buildComponent(LanguageContainer.getInstance().getRandomMessage(loc, id[0], id[1], id[2], param));
    }

    static Component examine(Component component, Locale locale) {
        var repl = TextReplacementConfig.builder()
                .match(Language.MESSAGE_PATTERN)
                .replacement((result, builder) -> searchResult(locale, result.group(), false))
                .build();
        var rand = TextReplacementConfig.builder()
                .match(Language.RANDOM_MESSAGE_PATTERN)
                .replacement((result, builder) -> searchResult(locale, result.group(), true))
                .build();

        return component.replaceText(rand).replaceText(repl);
    }
}
