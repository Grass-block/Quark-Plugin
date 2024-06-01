package org.tbstcraft.quark.util.query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TemplateEngine {
    public static final Pattern EXTRACT_PATTERN = Pattern.compile("\\{#(.*?)\\}");
    public static final String REPLACE_PATTERN = "{#%s}";

    private final QueryHandler queryHandler = new QueryHandler();

    public static List<String> extract(String input) {
        List<String> result = new ArrayList<>();
        Matcher matcher = EXTRACT_PATTERN.matcher(input);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    public String handle(String input) {
        return _handle(_handle(input));
    }

    public String _handle(String input){
        List<String> keys = extract(input);
        for (String s : keys) {
            String s2 = s.substring(2, s.length() - 1);
            String replacement = this.getQueryHandler().query(s2);
            if (replacement == null) {
                continue;
            }
            input = input.replace(s, replacement);
        }
        return input;
    }

    public void register(String id, Supplier<Object> supplier) {
        this.queryHandler.register(id, supplier);
    }

    public void unregister(String id) {
        this.queryHandler.unregister(id);
    }


    public QueryHandler getQueryHandler() {
        return queryHandler;
    }
}
