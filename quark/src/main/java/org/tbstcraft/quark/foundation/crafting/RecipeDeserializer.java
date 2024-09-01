package org.tbstcraft.quark.foundation.crafting;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
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

    static AttributeModifier createAttributeModifier(String line) {
        String[] args = line.split(";");
        return new AttributeModifier(args[2], Double.parseDouble(args[1]), switch (args[0]) {
            case "add" -> AttributeModifier.Operation.ADD_NUMBER;
            case "add-scale" -> AttributeModifier.Operation.ADD_SCALAR;
            case "mult-scalar-1" -> AttributeModifier.Operation.MULTIPLY_SCALAR_1;
            default -> throw new IllegalStateException("Unexpected value: " + args[0]);
        });
    }

    @SuppressWarnings("DataFlowIssue")
    static ItemStack createItem(ConfigurationSection parent, String name, String repeatType) {
        if (parent.isString(name)) {
            String[] outs = Objects.requireNonNull(parent.getString(name)).split("\\*");
            Material out = RecipeBuilder.resolveMaterial(outs[0].replace("{#type}", repeatType));

            ItemStack result = new ItemStack(out);
            if (outs.length > 1) {
                result.setAmount(Integer.parseInt(outs[1]));
            }

            return result;
        } else {
            ConfigurationSection stack = parent.getConfigurationSection(name);

            Material out = RecipeBuilder.resolveMaterial(stack.getString("type").replace("{#type}", repeatType));
            int count = stack.getInt("amount", 1);

            ItemStack result = new ItemStack(out, count);

            ItemMeta meta = result.getItemMeta();
            if (stack.contains("enchants")) {
                ConfigurationSection section = stack.getConfigurationSection("enchants");

                for (String s : section.getKeys(false)) {
                    var k = NamespacedKey.minecraft(s);
                    var l = section.getInt(s);
                    meta.addEnchant(Registry.ENCHANTMENT.get(k), l, true);
                }
            }

            if (stack.contains("attributes")) {
                ConfigurationSection section = stack.getConfigurationSection("attributes");
                for (String s : section.getKeys(false)) {
                    var k = NamespacedKey.minecraft(s.replace("/", "."));
                    Attribute attribute = Registry.ATTRIBUTE.get(k);
                    if (attribute == null) {
                        Quark.getInstance().getLogger().warning("skipped null attr %s".formatted(s));
                        continue;
                    }

                    meta.addAttributeModifier(attribute, createAttributeModifier(section.getString(s)));
                }
            }
            result.setItemMeta(meta);

            return result;
        }
    }


    static Recipe deserialize1(String repeatType, String id, ConfigurationSection section) {
        String[] outs = Objects.requireNonNull(section.getString("output")).split("\\*");
        NamespacedKey key = new NamespacedKey(Quark.getInstance(), id + "/" + repeatType);
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

    static Recipe deserialize(String repeatType, String id, ConfigurationSection section) {

        NamespacedKey key = new NamespacedKey(Quark.getInstance(), id + "/" + repeatType);
        String group = section.getString("group", "");

        ItemStack result = createItem(section, "output", repeatType);


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
                        ItemStack choice = createItem(map, s, repeatType);
                        recipe.setIngredient(sid, new RecipeChoice.ExactChoice(choice));
                    }
                }
                yield recipe;
            }
            case "shapeless" -> {
                ShapelessRecipe recipe = new ShapelessRecipe(key, result);
                recipe.setGroup(group);

                if (section.isList("requirements")) {
                    for (String data : section.getStringList("requirements")) {
                        if (data.startsWith("#")) {
                            NamespacedKey tagKey = Objects.requireNonNull(NamespacedKey.fromString(data.replace("#", "")));
                            Tag<Material> tag = Bukkit.getTag("blocks", tagKey, Material.class);
                            recipe.addIngredient(new RecipeChoice.MaterialChoice(Objects.requireNonNull(tag)));
                        } else {
                            recipe.addIngredient(RecipeBuilder.resolveMaterial(data));
                        }
                    }
                } else {
                    ConfigurationSection stacks = section.getConfigurationSection("requirements");
                    for (String k : Objects.requireNonNull(stacks).getKeys(false)) {
                        ItemStack choice = createItem(stacks, k, repeatType);
                        recipe.addIngredient(new RecipeChoice.ExactChoice(choice));
                    }
                }

                yield recipe;
            }
            case "stone_cutter" -> {
                if (section.isConfigurationSection("input")) {
                    ItemStack choice = createItem(section, "input", repeatType);
                    yield new StonecuttingRecipe(key, result, new RecipeChoice.ExactChoice(choice));
                }
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
