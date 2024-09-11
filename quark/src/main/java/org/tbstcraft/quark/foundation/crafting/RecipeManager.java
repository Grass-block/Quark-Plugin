package org.tbstcraft.quark.foundation.crafting;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.inventory.Recipe;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.internal.task.TaskService;

public interface RecipeManager {
    static void register(Recipe... recipes) {
        TaskService.global().run(() -> {
            for (Recipe r : recipes) {
                if (Bukkit.getRecipe(((Keyed) r).getKey()) != null) {
                    return;
                }
                Bukkit.addRecipe(r);
            }
        });
    }

    static void unregister(Recipe... recipes) {
        TaskService.global().run(() -> {
            if (APIProfileTest.isArclightBasedServer()) {
                return;
            }
            for (Recipe r : recipes) {
                Bukkit.removeRecipe(((Keyed) r).getKey());
            }
        });
    }
}
