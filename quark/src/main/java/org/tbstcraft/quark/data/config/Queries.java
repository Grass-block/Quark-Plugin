package org.tbstcraft.quark.data.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Queries {
    Map<String, String> ENVIRONMENT_VARS = new HashMap<>();
    Pattern ENV_PATTERN = Pattern.compile("\\{\\$(.*?)}");

    static void setEnvironmentVars(ConfigurationSection section) {
        ENVIRONMENT_VARS.clear();
        for (String s : section.getKeys(false)) {
            ENVIRONMENT_VARS.put(s, section.getString(s));
        }
    }

    static String applyEnvironmentVars(String input) {
        List<String> result = new ArrayList<>();
        Matcher matcher = ENV_PATTERN.matcher(input);
        while (matcher.find()) {
            result.add(matcher.group());
        }

        for (String s : result) {
            String s2 = s.substring(2, s.length() - 1);
            String replacement = ENVIRONMENT_VARS.get(s2);
            if (replacement == null) {
                continue;
            }
            input = input.replace(s, replacement);
        }
        return input;
    }
}
