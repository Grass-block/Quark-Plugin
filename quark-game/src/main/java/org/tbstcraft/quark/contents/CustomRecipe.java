package org.tbstcraft.quark.contents;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.util.crafting.RecipeManager;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@QuarkModule
public final class CustomRecipe extends PackageModule {
    private final Set<Recipe> recipes = new HashSet<>();

    @Override
    public void enable() {
        recipes.clear();
        ConfigurationSection map = this.getConfig().getConfigurationSection("recipes");
        if (map != null) {
            for (String s : map.getKeys(false)) {
                ConfigurationSection recipe = Objects.requireNonNull(map.getConfigurationSection(s));
                if (recipe.contains("repeat")) {
                    recipes.addAll(RecipeManager.deserializeRepeatable(s, recipe));
                } else {
                    recipes.add(RecipeManager.deserialize(s, recipe));
                }
            }
        }
        RecipeManager.register(this.recipes.toArray(new Recipe[0]));
    }

    @Override
    public void disable() {
        RecipeManager.register(this.recipes.toArray(new Recipe[0]));
    }
}
