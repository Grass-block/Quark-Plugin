package org.tbstcraft.quark.data.language;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
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
        try {
            if (sender instanceof Player player) {
                Locale locale = locale(player.getLocale());

                return Locale.CHINA;

                //todo why the hell is bukkit language detection inaccurate???

                //if (locale != Locale.ENGLISH) {
                    //return locale;
                //}
            } else {
                //todo:env lang opt
                return Locale.ENGLISH;
            }
        } catch (NoSuchMethodError error) {
            return Locale.CHINA;
        }
    }

    static Locale locale(String id) {
        return switch (id) {
            case "zh_cn" -> Locale.SIMPLIFIED_CHINESE;
            case "en_us", "en" -> Locale.US;
            default -> Locale.CHINA;
        };
    }

    static String locale(Locale locale) {
        if (locale == Locale.ENGLISH || locale == Locale.US) {
            return "en_us";
        }
        return locale.toLanguageTag().replace('-', '_').toLowerCase();
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
