package org.tbstcraft.quark.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TemplateExpansion {
    private final String[] map;

    public TemplateExpansion(String[] map) {
        this.map = map;
    }

    public static builder builder(){
        return new builder();
    }

    public static TemplateExpansion build(Consumer<builder> context){
        var builder = builder();
        context.accept(builder);
        return builder.build();
    }


    public String expand(String template, Object... args) {
        if (args.length != this.map.length) {
            throw new IllegalArgumentException("Argument count mismatch");
        }

        for (var i = 0; i < this.map.length; i++) {
            var find = "{" + this.map[i] + "}";

            template = template.replace(find, args[i].toString());
        }

        return template;
    }

    public static final class builder {
        private final List<String> cache = new ArrayList<>();

        public builder replacement(String name) {
            this.cache.add(name);
            return this;
        }

        public TemplateExpansion build() {
            return new TemplateExpansion(this.cache.toArray(new String[0]));
        }
    }
}
