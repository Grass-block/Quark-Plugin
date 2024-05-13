package org.tbstcraft.quark.contents;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.util.crafting.RecipeDeserializer;
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
                    recipes.addAll(RecipeDeserializer.deserializeRepeatable(s, recipe));
                } else {
                    recipes.add(RecipeDeserializer.deserialize(s, recipe));
                }
            }
        }
        RecipeManager.register(this.recipes.toArray(new Recipe[0]));
    }

    @Override
    public void disable() {
        RecipeManager.unregister(this.recipes.toArray(new Recipe[0]));
    }
}
