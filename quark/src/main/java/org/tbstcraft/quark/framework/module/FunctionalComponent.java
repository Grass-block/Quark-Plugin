package org.tbstcraft.quark.framework.module;

import org.bukkit.event.Listener;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;

public interface FunctionalComponent extends Listener {
    default void enable() {
    }

    default void disable() {
    }

    default void checkCompatibility() throws APIIncompatibleException {
    }
}
