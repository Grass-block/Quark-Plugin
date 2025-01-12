package org.atcraftmc.quark.management;

import me.gb2022.commons.reflect.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.LanguageContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.tbstcraft.quark.foundation.platform.PluginUtil;
import org.tbstcraft.quark.foundation.TextSender;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("removal")
@QuarkCommand(name = "plugins",aliases = "pl", op = true)
@QuarkModule(version = "1.1.0")
public final class AdvancedPluginCommand extends CommandModule {
    private final PluginUtil.ModernPluginManager pluginManager = PluginUtil.INSTANCE;
    private final PluginManager bukkitPluginManager = Bukkit.getPluginManager();

    @Inject
    private LanguageEntry language;


    private Component genMessage(String msg, Object... fmt) {
        return Component.text(ChatColor.translateAlternateColorCodes('&', msg.formatted(fmt)));
    }

    @Override
    public void execute(CommandExecution context) {
        switch (context.requireEnum(0, "load", "unload", "reload", "enable", "disable", "restart", "info", "list")) {
            case "load" -> this.loadPlugin(context.getSender(), context.requireArgumentAt(1));
            case "unload" -> this.unloadPlugin(context.getSender(), context.requireArgumentAt(1));
            case "reload" -> this.reloadPlugin(context.getSender(), context.requireArgumentAt(1));
            case "enable" -> this.enablePlugin(context.getSender(), context.requireArgumentAt(1));
            case "disable" -> this.disablePlugin(context.getSender(), context.requireArgumentAt(1));
            case "restart" -> this.restartPlugin(context.getSender(), context.requireArgumentAt(1));
            case "info" -> this.sendPluginInfo(context.getSender(), context.requireArgumentAt(1));
            case "list" -> this.listPlugins(context.getSender());
        }
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "load", "unload", "reload", "enable", "disable", "restart", "info", "list");

        var ctx = new Consumer<CommandSuggestion>() {
            @Override
            public void accept(CommandSuggestion c) {
                c.suggest(1, Arrays.stream(bukkitPluginManager.getPlugins()).map(Plugin::getName).collect(Collectors.toSet()));
            }
        };

        suggestion.matchArgument(0, "unload", ctx);
        suggestion.matchArgument(0, "reload", ctx);
        suggestion.matchArgument(0, "enable", ctx);
        suggestion.matchArgument(0, "disable", ctx);
        suggestion.matchArgument(0, "restart", ctx);
        suggestion.matchArgument(0, "info", ctx);

        suggestion.matchArgument(0, "load", (c) -> {
            for (File f : Objects.requireNonNull(new File(System.getProperty("user.dir") + "/plugins").listFiles())) {
                if (!f.getName().endsWith(".jar")) {
                    continue;
                }
                c.suggest(1, f.getName());
            }
        });
    }

    @Override
    public Command getCoveredCommand() {
        return new org.bukkit.command.defaults.PluginsCommand("plugins");
    }

    private void enablePlugin(CommandSender sender, String name) {
        var p = this.bukkitPluginManager.getPlugin(name);
        if (p == null) {
            this.language.sendMessage(sender, "plugin-not-found", name);
            return;
        }

        this.bukkitPluginManager.enablePlugin(p);
        this.language.sendMessage(sender, "enable", name);
    }

    private void disablePlugin(CommandSender sender, String name) {
        var p = this.bukkitPluginManager.getPlugin(name);
        if (p == null) {
            this.language.sendMessage(sender, "plugin-not-found", name);
            return;
        }

        this.bukkitPluginManager.disablePlugin(p);
        this.language.sendMessage(sender, "disable", name);
    }

    private void restartPlugin(CommandSender sender, String name) {
        var p = this.bukkitPluginManager.getPlugin(name);
        if (p == null) {
            this.language.sendMessage(sender, "plugin-not-found", name);
            return;
        }

        this.bukkitPluginManager.disablePlugin(p);
        this.bukkitPluginManager.enablePlugin(p);
        this.language.sendMessage(sender, "restart", name);
    }

    private void loadPlugin(CommandSender sender, String fileName) {
        File file = new File(System.getProperty("user.dir") + "/plugins/" + fileName);

        if (!file.exists()) {
            this.language.sendMessage(sender, "file-not-found", fileName);
            return;
        }

        if (this.pluginManager.load(fileName) != null) {
            this.language.sendMessage(sender, "load", fileName);
        } else {
            this.language.sendMessage(sender, "load-failed", fileName);
        }
    }

    private boolean unloadPlugin(CommandSender sender, String name) {
        var p = this.bukkitPluginManager.getPlugin(name);
        if (p == null) {
            this.language.sendMessage(sender, "plugin-not-found", name);
            return false;
        }

        if (this.pluginManager.unload(p)) {
            this.language.sendMessage(sender, "unload", name);
            return true;
        } else {
            this.language.sendMessage(sender, "unload-failed", name);
            return false;
        }
    }

    private void reloadPlugin(CommandSender sender, String name) {
        var p = this.bukkitPluginManager.getPlugin(name);
        if (p == null) {
            this.language.sendMessage(sender, "plugin-not-found", name);
            return;
        }

        if (!unloadPlugin(sender, name)) {
            return;
        }
        loadPlugin(sender, name);
    }

    private Component buildPluginHoverInfo(Plugin plugin) {
        var template = """
                &7Name: &b%s
                &7ID: &b%s
                &7Version: &d%s
                &7Authors: &f%s
                &7Website: &f%s
                &7Description: &f%s
                &f
                &7[click to view detail]
                """;

        var desc = plugin.getDescription();

        return genMessage(template,
                          desc.getPrefix() == null ? desc.getName() : desc.getPrefix(),
                          desc.getName(),
                          desc.getVersion(),
                          String.join(",", desc.getAuthors()),
                          desc.getWebsite(),
                          desc.getDescription()
                         );
    }

    private void listPlugins(CommandSender sender) {
        var template = "[%s&f]%s";

        this.language.sendMessage(sender, "list", "");

        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            var status = this.bukkitPluginManager.isPluginEnabled(p) ? "&aE" : "&cD";
            var name = p.getName();

            var msg = genMessage(template, status, name);
            msg = msg.hoverEvent(HoverEvent.showText(buildPluginHoverInfo(p)));
            msg = msg.clickEvent(ClickEvent.runCommand("/plugins info " + p.getDescription().getName()));

            TextSender.sendMessage(sender, msg);
        }
    }

    private void sendPluginInfo(CommandSender sender, String name) {
        Plugin pl = Bukkit.getPluginManager().getPlugin(name);
        if (pl == null) {
            this.sendExceptionMessage(sender);
            return;
        }
        PluginDescriptionFile desc = pl.getDescription();

        StringBuilder builder = new StringBuilder(512);

        LanguageEntry language = this.language;
        Locale locale = Language.locale(sender);
        builder.append("  ")
                .append(language.getMessage(locale,
                                            "info-name",
                                            desc.getName(),
                                            desc.getPrefix() == null ? desc.getName() : desc.getPrefix()
                                           ))
                .append("\n");
        builder.append("  ").append(language.getMessage(locale, "info-version", desc.getVersion())).append("\n");
        builder.append("  ").append(language.getMessage(locale, "info-author", Arrays.toString(desc.getAuthors().toArray()))).append("\n");
        builder.append("  ").append(language.getMessage(locale, "info-depend", Arrays.toString(desc.getDepend().toArray()))).append("\n");
        builder.append("  ")
                .append(language.getMessage(locale, "info-soft-depend", Arrays.toString(desc.getSoftDepend().toArray())))
                .append("\n");
        builder.append("  ").append(language.getMessage(locale, "info-api", desc.getAPIVersion())).append("\n");
        builder.append("  ").append(language.getMessage(locale, "info-main", desc.getMain())).append("\n");
        builder.append("  ")
                .append(language.getMessage(locale, "info-desc", desc.getDescription() == null ? "[empty]" : desc.getDescription()))
                .append("\n");

        /*
        //builder.append("  ").append(language.getMessage(locale, "info-libs")).append("\n");
        for (String s : desc.getLibraries()) {
            builder.append(ChatColor.GOLD).append("     - ").append(ChatColor.WHITE).append(s).append("\n");
        }
         */
        this.language.sendMessage(sender, "info", name, builder.toString());
    }
}