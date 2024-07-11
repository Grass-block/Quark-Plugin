package org.tbstcraft.quark.util.placeholder;

import net.kyori.adventure.text.ComponentLike;
import org.tbstcraft.quark.foundation.text.TextBuilder;

public interface StringPlaceHolder extends GlobalPlaceHolder {
    String getText();

    default ComponentLike get() {
        return TextBuilder.build(getText());
    }
}
