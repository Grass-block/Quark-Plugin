package org.atcraftmc.quark.contents;

import me.gb2022.commons.reflect.Inject;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Recipe;
import org.tbstcraft.quark.data.assets.AssetGroup;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.foundation.crafting.RecipeDeserializer;
import org.tbstcraft.quark.foundation.crafting.RecipeManager;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@QuarkModule(version = "1.2.6")
@CommandProvider(CustomRecipe.RecipeCommand.class)
public final class CustomRecipe extends PackageModule {
    private final Set<Recipe> recipes = new HashSet<>();

    @Inject("recipe;false")
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
                    getLanguage().sendMessage(sender, "reload");
                }
                case "restore" -> {
                    this.getModule().restoreDefault();
                    getLanguage().sendMessage(sender, "restore");
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
