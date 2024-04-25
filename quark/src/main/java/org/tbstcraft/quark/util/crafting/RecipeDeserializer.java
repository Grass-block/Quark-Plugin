package org.tbstcraft.quark.util.crafting;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.*;
import org.tbstcraft.quark.Quark;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public interface RecipeDeserializer {
    static Set<Recipe> deserializeRepeatable(String id, ConfigurationSection section) {
        Set<Recipe> sets = new HashSet<>();
        for (String s : section.getStringList("repeat")) {
            sets.add(deserialize(s, id, section));
        }
        return sets;
    }

    static Recipe deserialize(String id, ConfigurationSection section) {
        return deserialize("", id, section);
    }

    static Recipe deserialize(String repeatType, String id, ConfigurationSection section) {
        String[] outs = Objects.requireNonNull(section.getString("output")).split("\\*");
        NamespacedKey key = new NamespacedKey(Quark.PLUGIN, id + "/" + repeatType);
        String group = section.getString("group", "");

        Material out = RecipeBuilder.resolveMaterial(outs[0].replace("{#type}", repeatType));
        ItemStack result = new ItemStack(out, Integer.parseInt(outs[1]));

        return switch (Objects.requireNonNull(section.getString("type"))) {
            case "shaped" -> {
                ShapedRecipe recipe = new ShapedRecipe(key, result);
                recipe.setGroup(group);
                recipe.shape(Objects.requireNonNull(section.getString("shape")).split(";"));
                ConfigurationSection map = section.getConfigurationSection("map");
                Objects.requireNonNull(map);

                for (String s : map.getKeys(false)) {
                    String data = map.getString(s);
                    char sid = s.charAt(0);

                    if (data == null) {
                        throw new IllegalArgumentException("failed to match requirement data");
                    }

                    if (data.startsWith("#")) {
                        NamespacedKey tagKey = Objects.requireNonNull(NamespacedKey.fromString(data.replace("#", "")));
                        Tag<Material> tag = Bukkit.getTag("blocks", tagKey, Material.class);
                        recipe.setIngredient(sid, new RecipeChoice.MaterialChoice(Objects.requireNonNull(tag)));
                    } else {
                        recipe.setIngredient(sid, RecipeBuilder.resolveMaterial(data.replace("{#type}", repeatType)));
                    }
                }
                yield recipe;
            }
            case "shapeless" -> {
                ShapelessRecipe recipe = new ShapelessRecipe(key, result);
                recipe.setGroup(group);

                for (String data : section.getStringList("requirements")) {
                    if (data.startsWith("#")) {
                        NamespacedKey tagKey = Objects.requireNonNull(NamespacedKey.fromString(data.replace("#", "")));
                        Tag<Material> tag = Bukkit.getTag("blocks", tagKey, Material.class);
                        recipe.addIngredient(new RecipeChoice.MaterialChoice(Objects.requireNonNull(tag)));
                    } else {
                        recipe.addIngredient(RecipeBuilder.resolveMaterial(data.replace("{#type}", repeatType)));
                    }
                }
                yield recipe;
            }
            case "stone_cutter" -> {
                String data = section.getString("input");

                if (data == null) {
                    throw new IllegalArgumentException("failed to match requirement data");
                }

                if (data.startsWith("#")) {
                    NamespacedKey tagKey = Objects.requireNonNull(NamespacedKey.fromString(data.replace("#", "")));
                    Tag<Material> tag = Bukkit.getTag("blocks", tagKey, Material.class);
                    yield new StonecuttingRecipe(key, result, new RecipeChoice.MaterialChoice(Objects.requireNonNull(tag)));
                } else {
                    yield new StonecuttingRecipe(key, result, RecipeBuilder.resolveMaterial(data.replace("{#type}", repeatType)));
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + section.getString("type"));
        };
    }
}
