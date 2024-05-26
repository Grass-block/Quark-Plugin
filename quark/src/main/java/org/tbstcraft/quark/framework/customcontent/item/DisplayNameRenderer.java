package org.tbstcraft.quark.framework.customcontent.item;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public interface DisplayNameRenderer {


    Component render(ItemStack stack);



    Component compose(Component original,Component built);





}
