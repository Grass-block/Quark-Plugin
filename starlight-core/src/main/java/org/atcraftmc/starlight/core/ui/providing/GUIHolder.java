package org.atcraftmc.starlight.core.ui.providing;

import org.atcraftmc.starlight.core.ui.AbstractInventoryUI;

public interface GUIHolder<I extends AbstractInventoryUI> {

    I getCached(Object... args);


}
