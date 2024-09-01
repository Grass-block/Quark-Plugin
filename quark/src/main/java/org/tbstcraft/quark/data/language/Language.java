package org.tbstcraft.quark.data.language;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.data.config.ConfigEntry;
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
        return buildList(list, preprocessors);
    }

    @SafeVarargs
    static String generateTemplate(ConfigEntry entry, String id, Function<String, String>... preprocessors) {
        id = Identifiers.external(id);
        if (entry.isType(id, String.class)) {
            var result = entry.getString(id);
            for (Function<String, String> preprocessor : preprocessors) {
                result = preprocessor.apply(result);
            }
            return result;
        }

        List<String> list = entry.getList(id);
        return buildList(list, preprocessors);
    }

    static String buildList(List<String> list, Function<String, String>[] preprocessors) {
        String result = list2string(list);
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

    static String list2string(List<String> list) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String ss : list) {
            i++;
            sb.append(ss);
            if (i < list.size()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
