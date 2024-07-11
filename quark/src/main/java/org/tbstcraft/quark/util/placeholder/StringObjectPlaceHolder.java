package org.tbstcraft.quark.util.placeholder;

import net.kyori.adventure.text.ComponentLike;
import org.tbstcraft.quark.foundation.text.TextBuilder;

public interface StringObjectPlaceHolder<I> extends ObjectPlaceHolder<I> {
    String getText(I target);

    default ComponentLike get(I target) {
        return TextBuilder.build(getText(target));
    }
}
