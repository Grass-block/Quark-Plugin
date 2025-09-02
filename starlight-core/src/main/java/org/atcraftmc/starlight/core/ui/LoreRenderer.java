package org.atcraftmc.starlight.core.ui;

import net.kyori.adventure.text.Component;
import org.atcraftmc.starlight.core.ui.view.InventoryUIView;

import java.util.*;
import java.util.function.Function;

public interface LoreRenderer extends Function<InventoryUIView, List<Component>> {
    static LoreRenderer fixed(List<Component> l) {
        return (player -> l);
    }

    static LoreRenderer fixed(Component... l) {
        return (player -> List.of(l));
    }

    static LoreRenderer none() {
        return (player -> List.of());
    }

    static LoreRenderer forwarding(TextRenderer... renderers) {
        return player -> {
            var list = new ArrayList<Component>();

            for (var render : renderers) {
                list.add(render.apply(player));
            }

            return list;
        };
    }
}
