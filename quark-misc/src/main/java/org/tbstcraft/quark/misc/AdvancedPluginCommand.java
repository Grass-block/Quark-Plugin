package org.tbstcraft.quark.misc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.PluginsCommand;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.CommandModule;
import org.tbstcraft.quark.module.QuarkModule;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@QuarkCommand(name = "plugins", op = true)
@QuarkModule
public final class AdvancedPluginCommand extends CommandModule {
    public static boolean load(String file) {
        File f = new File(System.getProperty("user.dir") + "/plugins/" + file);
        Plugin p;
        try {
            p = Bukkit.getPluginManager().loadPlugin(f);
        } catch (InvalidPluginException | InvalidDescriptionException e) {
            Quark.LOGGER.severe(e.getMessage());
            return false;
        }
        if (p == null) {
            return false;
        }
        if (Bukkit.getPluginManager().isPluginEnabled(p.getName())) {
            return false;
        }
        p.onLoad();
        Bukkit.getPluginManager().enablePlugin(p);
        return true;
    }

    public boolean disable(String id) {
        Plugin p = Bukkit.getPluginManager().getPlugin(id);
        if (p == null) {
            return false;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled(p)) {
            return false;
        }
        Bukkit.getPluginManager().disablePlugin(p);
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "list" -> {
                StringBuilder sb = new StringBuilder();
                for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
                    if (Bukkit.getPluginManager().isPluginEnabled(p)) {
                        sb.append(ChatColor.GREEN);
                    } else {
                        sb.append(ChatColor.RED);
                    }
                    sb.append(p.getName())
                            .append(ChatColor.GRAY)
                            .append(" -> ")
                            .append(ChatColor.WHITE)
                            .append(p.getDescription().getVersion())
                            .append("\n");
                }
                this.getLanguage().sendMessageTo(sender, "list", sb.toString());
            }
            case "reload" -> {
                Plugin p = Bukkit.getPluginManager().getPlugin(args[1]);
                if (p == null) {
                    this.sendExceptionMessage(sender);
                    return false;
                }
                Bukkit.getPluginManager().disablePlugin(p);
                Bukkit.getPluginManager().enablePlugin(p);
                this.getLanguage().sendMessageTo(sender, "reload", args[1]);
            }
            case "load" -> {
                if (load(args[1])) {
                    this.getLanguage().sendMessageTo(sender, "enable", args[1]);
                    return false;
                }
                this.sendExceptionMessage(sender);
            }
            case "disable" -> {
                if (disable(args[1])) {
                    this.getLanguage().sendMessageTo(sender, "disable", args[1]);
                    return false;
                }
                this.sendExceptionMessage(sender);
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
        if (args.length == 1) {
            tabList.add("list");
            tabList.add("load");
            tabList.add("disable");
            tabList.add("info");
            tabList.add("reload");
            return;
        }
        if (args.length == 2) {
            switch (args[0]) {
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
                    Collections.sort(tabList);
                }
                default -> {
                }
            }
        }
    }

    @Override
    public Command getCoveredCommand() {
        return new PluginsCommand("plugins");
    }
}