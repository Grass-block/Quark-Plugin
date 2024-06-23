package org.tbstcraft.quark.internal.ui.builder;

import org.bukkit.entity.Player;
import org.tbstcraft.quark.internal.ui.UI;

import java.util.function.Consumer;

public abstract class UIBuilder {
    Consumer<Player> initializer;

    static InventoryUIBuilder inventory(int slots) {
        return new InventoryUIBuilder(slots);
    }

    public abstract UI build();

    public UIBuilder initializer(Consumer<Player> initializer) {
        this.initializer = initializer;
        return this;
    }
}
