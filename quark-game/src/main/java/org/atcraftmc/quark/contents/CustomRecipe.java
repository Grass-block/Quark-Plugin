package org.atcraftmc.quark.contents;

import me.gb2022.commons.reflect.Inject;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Recipe;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.data.assets.AssetGroup;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.crafting.RecipeDeserializer;
import org.atcraftmc.starlight.foundation.crafting.RecipeManager;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@SLModule(version = "1.2.6")
@CommandProvider(CustomRecipe.RecipeCommand.class)
public final class CustomRecipe extends PackageModule {
    private final Set<Recipe> recipes = new HashSet<>();

    @Inject("recipe;false")
    private AssetGroup recipeFiles;

    @Inject
    private Logger logger;

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireClass(() -> Class.forName("org.bukkit.inventory.RecipeChoice"));
    }

    public void load() {
        this.clear();

        for (String s : this.recipeFiles.list()) {
            this.logger.info("loading recipe bundle %s".formatted(s));

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
                    MessageAccessor.send(this.getLanguage(), sender, "reload");
                }
                case "restore" -> {
                    this.getModule().restoreDefault();
                    MessageAccessor.send(this.getLanguage(), sender, "restore");
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
