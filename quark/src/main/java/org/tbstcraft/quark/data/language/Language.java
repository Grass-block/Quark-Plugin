package org.tbstcraft.quark.data.language;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.internal.LocaleService;
import org.tbstcraft.quark.util.Identifiers;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

public interface Language {
    Pattern MESSAGE_PATTERN = Pattern.compile("\\{msg#(.*?)}");
    Pattern RANDOM_MESSAGE_PATTERN = Pattern.compile("\\{rand#(.*?)}");
    Pattern LOCALIZED_GLOBAL_VAR = Pattern.compile("\\{global#(.*?)}");

    static Locale locale(CommandSender sender) {
        return LocaleService.locale(sender);
    }

    static Locale locale(String id) {
        return LocaleMapping.locale(id);
    }

    static String locale(Locale locale) {
        return LocaleMapping.minecraft(locale);
    }


    @SafeVarargs
    static String generateTemplate(ConfigurationSection section, String id, Function<String, String>... preprocessors) {
        id = Identifiers.external(id);
        if (section.isString(id)) {
            return section.getString(id);
        }

        List<String> list = section.getStringList(id);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String s2 : list) {
            i++;
            sb.append(s2);
            if (i < list.size()) {
                sb.append("\n");
            }
        }

        String result = sb.toString();
        for (Function<String, String> preprocessor : preprocessors) {
            result = preprocessor.apply(result);
        }

        return result;
    }

    static String format(String s, Object... format) {
        for (int i = 0; i < format.length; i++) {
            s = s.replace("{" + i + "}", format[i].toString());
        }
        return s.formatted(format);
    }
}
