package org.atcraftmc.starlight.core.custom.item;

import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface DisplayNameComposer {
    Component compose(Component original,Component built);
}
