package org.tbstcraft.quark.util.crafting;

import org.bukkit.*;
import org.bukkit.inventory.*;

public interface RecipeManager {
    static void register(Recipe... recipes) {
        for (Recipe r : recipes) {
            if (Bukkit.getRecipe(((Keyed) r).getKey()) != null) {
                Bukkit.removeRecipe(((Keyed) r).getKey());
            }
            Bukkit.addRecipe(r);
        }
    }

    static void unregister(Recipe... recipes) {
        for (Recipe r : recipes) {
            Bukkit.removeRecipe(((Keyed) r).getKey());
        }
    }


}
