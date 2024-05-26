package org.tbstcraft.quark.framework.customcontent.item;

import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface DisplayNameComposer {
    Component compose(Component original,Component built);
}
