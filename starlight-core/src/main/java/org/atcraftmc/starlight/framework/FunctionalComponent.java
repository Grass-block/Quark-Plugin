package org.atcraftmc.starlight.framework;

import org.bukkit.event.Listener;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;

public interface FunctionalComponent extends Listener {
    default void enable() {
    }

    default void disable() {
    }

    default void checkCompatibility() throws APIIncompatibleException {
    }
}
