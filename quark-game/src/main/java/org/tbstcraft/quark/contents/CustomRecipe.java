package org.tbstcraft.quark.contents;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Recipe;
import org.tbstcraft.quark.framework.command.CommandProvider;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.data.assets.AssetGroup;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.util.crafting.RecipeDeserializer;
import org.tbstcraft.quark.util.crafting.RecipeManager;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@QuarkModule(version = "1.2.6")
@CommandProvider(CustomRecipe.RecipeCommand.class)
public final class CustomRecipe extends PackageModule {
    private final Set<Recipe> recipes = new HashSet<>();
    private AssetGroup recipeFiles;

    public void load() {
        this.clear();

        for (String s : this.recipeFiles.list()) {
            this.getLogger().info("loading recipe bundle %s".formatted(s));

            ConfigurationSection map = YamlConfiguration.loadConfiguration(this.recipeFiles.getFile(s)).getConfigurationSection("recipes");

            if (map == null) {
                continue;
            }
            for (String id : map.getKeys(false)) {
                ConfigurationSection recipe = Objects.requireNonNull(map.getConfigurationSection(id));
                if (recipe.contains("repeat")) {
                    this.recipes.addAll(RecipeDeserializer.deserializeRepeatable(id, recipe));
                } else {
                    Recipe r = RecipeDeserializer.deserialize(id, recipe);
                    this.recipes.add(r);
                }
            }
            RecipeManager.register(this.recipes.toArray(new Recipe[0]));
        }
    }

    public void clear() {
        RecipeManager.unregister(this.recipes.toArray(new Recipe[0]));
        recipes.clear();
    }

    public void restoreDefault() {
        this.recipeFiles.save("vanilla-fixes.yml");
        this.recipeFiles.save("wood-cuttings.yml");
    }

    @Override
    public void enable() {
        this.recipeFiles = new AssetGroup(this.getOwnerPlugin(), "recipe", false);
        if (!this.recipeFiles.existFolder()) {
            this.restoreDefault();
        }
        this.load();
    }

    @Override
    public void disable() {
        this.clear();
    }

    @QuarkCommand(name = "recipes", permission = "-quark.recipe")
    public static final class RecipeCommand extends ModuleCommand<CustomRecipe> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            switch (args[0]) {
                case "reload" -> {
                    this.getModule().load();
                    getLanguage().sendMessageTo(sender, "reload");
                }
                case "restore" -> {
                    this.getModule().restoreDefault();
                    getLanguage().sendMessageTo(sender, "restore");
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("reload");
                tabList.add("restore");
            }
        }
    }
}
