package org.tbstcraft.quark.foundation.text;

import me.gb2022.commons.container.ThreadLocalStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public interface TextBuilder {
    ThreadLocalStorage<ComponentBlockBuilder> THREAD_LOCAL_STORAGE = new ThreadLocalStorage<>(ComponentBlockBuilder::new);
    String EMPTY_COMPONENT = "{;}";

    static ComponentBlock build(String s, Component... format) {
        return THREAD_LOCAL_STORAGE.get().build(EMPTY_COMPONENT + s, format);
    }

    static ComponentBlock build(String s, boolean checkURLFully, Component... format) {
        return THREAD_LOCAL_STORAGE.get().build(EMPTY_COMPONENT + s, checkURLFully, format);
    }

    static Component buildComponent(String s, Component... format) {
        return build(s, format).toSingleLine();
    }

    static Component buildComponent(String s, boolean checkURLFully, Component... format) {
        return build(s, checkURLFully, format).toSingleLine();
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
