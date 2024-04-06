package org.tbstcraft.quark.util.crafting;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.*;
import org.tbstcraft.quark.Quark;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

    static ShapedRecipe shaped(String id, String map, ItemStack result, PatternSymbol... symbols) {
        return shaped("", id, map, result, symbols);
    }

    static ShapedRecipe shaped(String group, String id, String map, ItemStack result, PatternSymbol... symbols) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(Quark.PLUGIN, id), result);
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
        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(Quark.PLUGIN, id), result);
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
        StonecuttingRecipe recipe = new StonecuttingRecipe(new NamespacedKey(Quark.PLUGIN, id), out, in);
        recipe.setGroup(group);
        return recipe;
    }

    static PatternSymbol symbol(char c, Material material) {
        return new PatternSymbol(c, material);
    }

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

        Material out = resolveMaterial(outs[0].replace("{#type}", repeatType));
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
                        recipe.setIngredient(sid, resolveMaterial(data.replace("{#type}", repeatType)));
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
                        recipe.addIngredient(resolveMaterial(data.replace("{#type}", repeatType)));
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
                    yield new StonecuttingRecipe(key, result, resolveMaterial(data.replace("{#type}", repeatType)));
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + section.getString("type"));
        };
    }

    static Material resolveMaterial(String data) {
        Material out = Material.matchMaterial(data);
        if (out == null) {
            throw new IllegalArgumentException("can not find: " + data);
        }
        return out;
    }

    class PatternSymbol {
        private final char id;
        private final Material require;

        PatternSymbol(char id, Material require) {
            this.id = id;
            this.require = require;
        }

        public char getId() {
            return id;
        }

        public Material getRequire() {
            return require;
        }
    }
}
