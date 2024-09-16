package org.atcraftmc.quark.management;

import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.PluginUtil;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings("removal")
@QuarkCommand(name = "plugins", op = true)
@QuarkModule(version = "1.1.0")
public final class AdvancedPluginCommand extends CommandModule {
    @Inject
    private LanguageEntry language;

    private void listPlugins(CommandSender sender) {
        StringBuilder sb = new StringBuilder();
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            if (Bukkit.getPluginManager().isPluginEnabled(p)) {
                sb.append(ChatColor.GREEN);
            } else {
                sb.append(ChatColor.RED);
            }
            sb.append(p.getName()).append(ChatColor.GRAY).append(" -> ").append(ChatColor.WHITE).append(p.getDescription().getVersion()).append("\n");
        }
        this.language.sendMessage(sender, "list", sb.toString());
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
        builder.append("  ").append(language.getMessage(locale, "info-name", desc.getName(), desc.getPrefix())).append("\n");
        builder.append("  ").append(language.getMessage(locale, "info-version", desc.getVersion())).append("\n");
        builder.append("  ").append(language.getMessage(locale, "info-author", Arrays.toString(desc.getAuthors().toArray()))).append("\n");
        builder.append("  ").append(language.getMessage(locale, "info-desc", desc.getDescription())).append("\n");
        builder.append("  ").append(language.getMessage(locale, "info-depend", Arrays.toString(desc.getDepend().toArray()))).append("\n");
        builder.append("  ").append(language.getMessage(locale, "info-soft-depend", Arrays.toString(desc.getSoftDepend().toArray()))).append("\n");
        builder.append("  ").append(language.getMessage(locale, "info-api", desc.getAPIVersion())).append("\n");
        builder.append("  ").append(language.getMessage(locale, "info-main", desc.getMain())).append("\n");
        builder.append("  ").append(language.getMessage(locale, "info-libs")).append("\n");
        for (String s : desc.getLibraries()) {
            builder.append(ChatColor.GOLD).append("     - ").append(ChatColor.WHITE).append(s).append("\n");
        }
        this.language.sendMessage(sender, "info", name, builder.toString());
    }


    private void disablePlugin(CommandSender sender, String name) {
        Bukkit.getPluginManager().disablePlugin(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(name)));
        this.language.sendMessage(sender, "disable", name);
    }

    private void enablePlugin(CommandSender sender, String name) {
        Bukkit.getPluginManager().enablePlugin(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(name)));
        this.language.sendMessage(sender, "enable", name);
    }


    private void unloadPlugin(CommandSender sender, String name) {
        if (PluginUtil.unload(name)) {
            this.language.sendMessage(sender, "unload", name);
            return;
        }
        this.sendExceptionMessage(sender);
    }

    private void loadPlugin(CommandSender sender, String name) {
        if (PluginUtil.load(name) == null) {
            this.language.sendMessage(sender, "enable", name);
            return;
        }
        this.sendExceptionMessage(sender);
    }

    private void reloadPlugin(CommandSender sender, String name) {
        Plugin p = Bukkit.getPluginManager().getPlugin(name);
        if (p == null) {
            this.sendExceptionMessage(sender);
            return;
        }
        PluginUtil.reload(name);
        this.language.sendMessage(sender, "reload", name);
    }


    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "list" -> this.listPlugins(sender);
            case "reload" -> this.reloadPlugin(sender, args[1]);
            case "load" -> this.loadPlugin(sender, args[1]);
            case "unload" -> this.unloadPlugin(sender, args[1]);
            case "enable" -> this.enablePlugin(sender, args[1]);
            case "disable" -> this.disablePlugin(sender, args[1]);
            case "info" -> this.sendPluginInfo(sender, args[1]);
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("list");
            tabList.add("load");
            tabList.add("unload");
            tabList.add("info");
            tabList.add("reload");
            tabList.add("enable");
            tabList.add("disable");
            return;
        }

        if (buffer.length == 2) {
            switch (buffer[0]) {
                case "disable", "info", "reload" -> {
                    for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
                        tabList.add(p.getName());
                    }
                }
                case "load" -> {
                    for (File f : Objects.requireNonNull(new File(System.getProperty("user.dir") + "/plugins").listFiles())) {
                        if (!f.getName().endsWith(".jar")) {
                            continue;
                        }
                        tabList.add(f.getName());
                    }
                }
            }
        }
    }

    @Override
    public Command getCoveredCommand() {
        return new org.bukkit.command.defaults.PluginsCommand("plugins");
    }
}