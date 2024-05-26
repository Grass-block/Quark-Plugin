package org.tbstcraft.quark.util.crafting;

import org.bukkit.*;
import org.bukkit.inventory.*;
import org.tbstcraft.quark.util.platform.APIProfileTest;

public interface RecipeManager {
    static void register(Recipe... recipes) {
        for (Recipe r : recipes) {
            if (Bukkit.getRecipe(((Keyed) r).getKey()) != null) {
                return;
            }
            Bukkit.addRecipe(r);
        }
    }

    static void unregister(Recipe... recipes) {
        if(APIProfileTest.isArclightBasedServer()){
            return;
        }
        for (Recipe r : recipes) {
            Bukkit.removeRecipe(((Keyed) r).getKey());
        }
    }
}
