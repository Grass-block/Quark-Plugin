package org.atcraftmc.starlight.sideload;

import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.reflect.Inject;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.starlight.Configurations;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.foundation.crafting.RecipeBuilder;
import org.atcraftmc.starlight.foundation.crafting.RecipeManager;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Supplier;

@SLModule
public final class RecipeLoader extends PackageModule {
    private final Map<String, RecipeDispatcher> dispatchers = new HashMap<>();
    private final Set<Recipe> recipes = new HashSet<>();

    @Inject
    private Logger logger;

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireClass(() -> Class.forName("org.bukkit.inventory.RecipeChoice"));
    }

    @Override
    @SuppressWarnings("Convert2MethodRef")
    public void enable() {
        this.recipes.clear();

        register("shaped", () -> Class.forName("org.bukkit.inventory.ShapedRecipe"), () -> new ShapedDispatcher());
        register("shapeless", () -> Class.forName("org.bukkit.inventory.ShapelessRecipe"), () -> new ShapelessDispatcher());
        register("stone-cutter", () -> Class.forName("org.bukkit.inventory.StonecuttingRecipe"), () -> new StoneCutterDispatcher());

        this.dispatchers.putAll(PluginMessenger.queryMapped("starlight:recipe-dispatcher", (b) -> b.put("dispatchers", this.dispatchers)).getProperty("dispatchers", Map.class));

        Configurations.groupedYML("recipes", Set.of("recipe-bundle.yml")).forEach((id, dom) -> {
            this.logger.info("loading recipe bundle {}", id);

            if (dom == null) {
                return;
            }
            for (var k : dom.getKeys(false)) {
                var recipe = Objects.requireNonNull(dom.getConfigurationSection(k));
                if (recipe.contains("repeat")) {
                    this.recipes.addAll(deserializeRepeatable(k, recipe));
                } else {
                    var r = deserialize(k, recipe);
                    this.recipes.add(r);
                }
            }
        });

        RecipeManager.register(this.recipes.toArray(new Recipe[0]));
    }

    @Override
    public void disable() {
        RecipeManager.unregister(this.recipes.toArray(new Recipe[0]));
        this.recipes.clear();
    }

    public void register(String id, Compatibility.ClassAssertion a, Supplier<RecipeDispatcher> handler) {
        try {
            a.get();
            this.dispatchers.put(id, handler.get());
        } catch (ClassNotFoundException ignored) {
        }
    }

    private Set<Recipe> deserializeRepeatable(String id, ConfigurationSection section) {
        var sets = new HashSet<Recipe>();
        for (String s : section.getStringList("repeat")) {
            sets.add(deserialize(s, id, section));
        }
        return sets;
    }

    private Recipe deserialize(String id, ConfigurationSection section) {
        return deserialize("", id, section);
    }

    private Recipe deserialize(String repeatType, String id, ConfigurationSection section) {
        var key = new NamespacedKey(Starlight.instance(), id + "/" + repeatType);
        var group = section.getString("group", "");
        var result = RecipeDispatcher.createItem(section, "output", repeatType);

        var dispatcher = this.dispatchers.get(Objects.requireNonNull(section.getString("type")));

        if (dispatcher == null) {
            throw new IllegalArgumentException("Unknown recipe type: " + section.getString("type"));
        }

        return dispatcher.deserialize(key, group, result, section, repeatType);
    }


    @FunctionalInterface
    public interface RecipeDispatcher {
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
                            Starlight.instance().getLogger().warning("skipped null attr %s".formatted(s));
                            continue;
                        }

                        meta.addAttributeModifier(attribute, createAttributeModifier(section.getString(s)));
                    }
                }
                result.setItemMeta(meta);

                return result;
            }
        }

        Recipe deserialize(NamespacedKey key, String group, ItemStack result, ConfigurationSection section, String repeatType);
    }

    static final class ShapedDispatcher implements RecipeDispatcher {

        @Override
        public Recipe deserialize(NamespacedKey key, String group, ItemStack result, ConfigurationSection section, String repeatType) {
            var recipe = new ShapedRecipe(key, result);
            var map = Objects.requireNonNull(section.getConfigurationSection("map"));

            recipe.setGroup(group);
            recipe.shape(Objects.requireNonNull(section.getString("shape")).split(";"));

            for (var s : map.getKeys(false)) {
                var data = map.getString(s);
                var sid = s.charAt(0);

                if (data == null) {
                    throw new IllegalArgumentException("failed to match requirement data");
                }

                if (data.startsWith("#")) {
                    var tagKey = Objects.requireNonNull(NamespacedKey.fromString(data.replace("#", "")));
                    var tag = Bukkit.getTag("blocks", tagKey, Material.class);
                    recipe.setIngredient(sid, new RecipeChoice.MaterialChoice(Objects.requireNonNull(tag)));
                } else {
                    var choice = RecipeDispatcher.createItem(map, s, repeatType);
                    recipe.setIngredient(sid, new RecipeChoice.ExactChoice(choice));
                }
            }

            return recipe;
        }
    }

    static final class ShapelessDispatcher implements RecipeDispatcher {
        @Override
        public Recipe deserialize(NamespacedKey key, String group, ItemStack result, ConfigurationSection section, String repeatType) {
            var recipe = new ShapelessRecipe(key, result);
            recipe.setGroup(group);

            for (String data : section.getStringList("requirements")) {
                if (data.startsWith("#")) {
                    var tagKey = Objects.requireNonNull(NamespacedKey.fromString(data.replace("#", "")));
                    var tag = Bukkit.getTag("blocks", tagKey, Material.class);
                    recipe.addIngredient(new RecipeChoice.MaterialChoice(Objects.requireNonNull(tag)));
                } else {
                    recipe.addIngredient(RecipeBuilder.resolveMaterial(data));
                }
            }

            return recipe;
        }
    }

    static final class StoneCutterDispatcher implements RecipeDispatcher {

        @Override
        public Recipe deserialize(NamespacedKey key, String group, ItemStack result, ConfigurationSection section, String repeatType) {
            if (section.isConfigurationSection("input")) {
                var choice = RecipeDispatcher.createItem(section, "input", repeatType);
                return new StonecuttingRecipe(key, result, new RecipeChoice.ExactChoice(choice));
            }
            var data = section.getString("input");

            if (data == null) {
                throw new IllegalArgumentException("failed to match requirement data");
            }

            if (data.startsWith("#")) {
                var tagKey = Objects.requireNonNull(NamespacedKey.fromString(data.replace("#", "")));
                var tag = Bukkit.getTag("blocks", tagKey, Material.class);
                return new StonecuttingRecipe(key, result, new RecipeChoice.MaterialChoice(Objects.requireNonNull(tag)));
            } else {
                return new StonecuttingRecipe(key, result, RecipeBuilder.resolveMaterial(data.replace("{#type}", repeatType)));
            }
        }
    }
}
