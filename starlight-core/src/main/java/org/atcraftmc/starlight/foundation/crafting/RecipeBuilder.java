package org.atcraftmc.starlight.foundation.crafting;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.StonecuttingRecipe;
import org.atcraftmc.starlight.Starlight;

public interface RecipeBuilder {
    static ShapedRecipe shaped(String id, String map, ItemStack result, PatternSymbol... symbols) {
        return shaped("", id, map, result, symbols);
    }

    static ShapedRecipe shaped(String group, String id, String map, ItemStack result, PatternSymbol... symbols) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(Starlight.instance(), id), result);
        recipe.shape(map.split(";"));
        for (PatternSymbol symbol : symbols) {
            recipe.setIngredient(symbol.getId(), symbol.getRequire());
        }
        recipe.setGroup(group);
        return recipe;
    }

    static ShapelessRecipe shapeLess(String id, ItemStack result, Material... require) {
        return shapeLess("", id, result, require);
    }

    static ShapelessRecipe shapeLess(String group, String id, ItemStack result, Material... require) {
        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(Starlight.instance(), id), result);
        recipe.setGroup(group);
        for (Material m : require) {
            recipe.addIngredient(m);
        }
        return recipe;
    }

    static StonecuttingRecipe stoneCutter(String id, Material in, ItemStack out) {
        return stoneCutter("", id, in, out);
    }

    static StonecuttingRecipe stoneCutter(String group, String id, Material in, ItemStack out) {
        StonecuttingRecipe recipe = new StonecuttingRecipe(new NamespacedKey(Starlight.instance(), id), out, in);
        recipe.setGroup(group);
        return recipe;
    }

    static PatternSymbol symbol(char c, Material material) {
        return new PatternSymbol(c, material);
    }

    static Material resolveMaterial(String data) {
        Material out = Material.matchMaterial(data);
        if (out == null) {
            throw new IllegalArgumentException("can not find: " + data);
        }
        return out;
    }
}
