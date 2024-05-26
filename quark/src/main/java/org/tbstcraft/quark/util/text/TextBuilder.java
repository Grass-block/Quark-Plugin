package org.tbstcraft.quark.util.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.tbstcraft.quark.util.ThreadLocalStorage;

import java.util.ArrayList;
import java.util.List;

public interface TextBuilder {
    ThreadLocalStorage<ComponentBlockBuilder> THREAD_LOCAL_STORAGE = new ThreadLocalStorage<>(ComponentBlockBuilder::new);

    static ComponentBlock build(String s, Component... format) {
        return THREAD_LOCAL_STORAGE.get().build("{}" + s, format);
    }

    static Component buildComponent(String s, Component... format) {
        return build(s, format).toSingleLine();
    }

    static String buildString(String s, Component... format) {
        return LegacyComponentSerializer.legacySection().serialize(buildComponent(s, format));
    }

    static List<String> buildStringBlocks(String s, Component... format) {
        List<String> list = new ArrayList<>();
        for (Component c : build(s, format)) {
            list.add(LegacyComponentSerializer.legacySection().serialize(c));
        }
        return list;
    }
}
