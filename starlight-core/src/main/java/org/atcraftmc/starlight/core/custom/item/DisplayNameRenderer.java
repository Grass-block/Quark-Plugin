package org.atcraftmc.starlight.core.custom.item;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public interface DisplayNameRenderer {


    Component render(ItemStack stack);



    Component compose(Component original,Component built);





}
