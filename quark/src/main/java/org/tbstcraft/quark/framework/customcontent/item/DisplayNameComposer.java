package org.tbstcraft.quark.framework.customcontent.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.util.api.APIProfileTest;

@FunctionalInterface
public interface DisplayNameComposer {
    Component compose(Component original,Component built);
}
