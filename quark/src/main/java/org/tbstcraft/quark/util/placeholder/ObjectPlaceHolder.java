package org.tbstcraft.quark.util.placeholder;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public interface ObjectPlaceHolder<I> {
    ComponentLike get(I target);

    default String getText(I target) {
        return LegacyComponentSerializer.legacySection().serialize(get(target).asComponent());
    }
}
