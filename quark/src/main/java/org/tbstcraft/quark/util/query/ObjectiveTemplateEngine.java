package org.tbstcraft.quark.util.query;

import java.util.List;
import java.util.function.Function;

public class ObjectiveTemplateEngine<T> {
    private final ObjectiveQueryHandler<T> queryHandler = new ObjectiveQueryHandler<>();

    public String handle(T obj, String input) {
        List<String> keys = TemplateEngine.extract(input);
        for (String s : keys) {
            String s2 = s.substring(2, s.length() - 1);
            String replacement = this.getQueryHandler().query(obj, s2);
            if (replacement == null) {
                continue;
            }
            input = input.replace(s, replacement);
        }
        return input;
    }

    public void register(String id, Function<T, String> supplier) {
        this.queryHandler.register(id, supplier);
    }

    public void unregister(String id) {
        this.queryHandler.unregister(id);
    }

    public ObjectiveQueryHandler<T> getQueryHandler() {
        return queryHandler;
    }
}
