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
                    continue;
                }
                try {
                    Bukkit.addRecipe(r);
                } catch (IllegalArgumentException e) {
                    if (APIProfileTest.isMixedServer()) {
                        continue;
                    }

                    throw e;
                }
            }
        });
    }

    static void unregister(Recipe... recipes) {
        if (APIProfileTest.isMixedServer()) {
            return;
        }

        TaskService.global().run(() -> {
            for (Recipe r : recipes) {
                Bukkit.removeRecipe(((Keyed) r).getKey());
            }
        });
    }
}
