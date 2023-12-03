package org.tbstcraft.quark.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.SharedContext;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.config.ConfigFile;
import org.tbstcraft.quark.config.LanguageFile;
import org.tbstcraft.quark.module.ModuleManager;
import org.tbstcraft.quark.module.ModuleStatus;
import org.tbstcraft.quark.service.PlayerAuthService;

import java.util.List;
import java.util.Objects;

public interface InternalCommands {
    ModuleCommand MODULE_COMMAND = new ModuleCommand();
    LanguageCommand LANGUAGE_COMMAND = new LanguageCommand();
    ConfigCommand CONFIG_COMMAND = new ConfigCommand();
    SetPasswordCommand SET_PASSWORD_COMMAND = new SetPasswordCommand();

    static void register() {
        CommandManager.registerCommand(MODULE_COMMAND);
        CommandManager.registerCommand(LANGUAGE_COMMAND);
        CommandManager.registerCommand(CONFIG_COMMAND);
        CommandManager.registerCommand(SET_PASSWORD_COMMAND);
    }

    static void unregister() {
        CommandManager.unregisterCommand(MODULE_COMMAND);
        CommandManager.unregisterCommand(LANGUAGE_COMMAND);
        CommandManager.unregisterCommand(CONFIG_COMMAND);
        CommandManager.unregisterCommand(SET_PASSWORD_COMMAND);
    }

    @QuarkCommand(name = "configs", op = true)
    class ConfigCommand extends CoreCommand {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            switch (args[0]) {
                case "reload_all" -> {
                    ConfigFile.reloadAll();
                    Quark.LANGUAGE.sendMessageTo(sender, "config", "reload_all");
                }
                case "restore_all" -> {
                    ConfigFile.restoreAll();
                    Quark.LANGUAGE.sendMessageTo(sender, "config", "restore_all");
                }
                case "restore" -> {
                    ConfigFile file = ConfigFile.REGISTERED_CACHE.get(args[1]);
                    if (file == null) {
                        this.sendExceptionMessage(sender);
                        return;
                    }
                    file.restore();
                    Quark.LANGUAGE.sendMessageTo(sender, "config", "restore", args[1]);
                }
                case "reload" -> {
                    ConfigFile file = ConfigFile.REGISTERED_CACHE.get(args[1]);
                    if (file == null) {
                        this.sendExceptionMessage(sender);
                        return;
                    }
                    file.reload();
                    Quark.LANGUAGE.sendMessageTo(sender, "config", "reload", args[1]);
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
            if (args.length == 1) {
                tabList.add("reload");
                tabList.add("restore");
                tabList.add("reload_all");
                tabList.add("restore_all");
                return;
            }
            if (args.length == 2 && !args[0].contains("_all")) {
                tabList.addAll(ConfigFile.REGISTERED_CACHE.keySet());
            }
        }
    }

    @QuarkCommand(name = "language", op = true)
    class LanguageCommand extends CoreCommand {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            switch (args[0]) {
                case "reload_global_vars" -> {
                    SharedObjects.loadGlobalVars();
                    Quark.LANGUAGE.sendMessageTo(sender, "config", "reload_global_vars");
                }
                case "reload_all" -> {
                    LanguageFile.reloadAll();
                    Quark.LANGUAGE.sendMessageTo(sender, "language", "reload_all");
                }
                case "restore_all" -> {
                    LanguageFile.restoreAll();
                    Quark.LANGUAGE.sendMessageTo(sender, "language", "restore_all");
                }
                case "restore" -> {
                    LanguageFile file = LanguageFile.REGISTERED_CACHE.get(args[1]);
                    if (file == null) {
                        this.sendExceptionMessage(sender);
                        return;
                    }
                    file.restore();
                    Quark.LANGUAGE.sendMessageTo(sender, "language", "restore", args[1]);
                }
                case "reload" -> {
                    LanguageFile file = LanguageFile.REGISTERED_CACHE.get(args[1]);
                    if (file == null) {
                        this.sendExceptionMessage(sender);
                        return;
                    }
                    file.reload();
                    Quark.LANGUAGE.sendMessageTo(sender, "language", "reload", args[1]);
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
            if (args.length == 1) {
                tabList.add("reload");
                tabList.add("restore");
                tabList.add("reload_all");
                tabList.add("restore_all");
                tabList.add("reload_global_vars");
                return;
            }
            if (args.length == 2 && !args[0].contains("_all")) {
                tabList.addAll(LanguageFile.REGISTERED_CACHE.keySet());
            }
        }
    }

    @QuarkCommand(name = "module", op = true)
    class ModuleCommand extends CoreCommand {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            switch (args[0]) {
                case "list_all" -> {
                    Quark.LANGUAGE.sendMessageTo(sender, "module", "list");
                    for (String s : ModuleManager.MODULES.keySet().stream().sorted().toList()) {
                        if (ModuleManager.getModuleStatus(s) == ModuleStatus.ENABLED) {
                            sender.sendMessage(ChatColor.GREEN + s);
                            continue;
                        }
                        sender.sendMessage(ChatColor.GRAY + s);

                    }
                }
                case "enable_all" -> {
                    ModuleManager.enableAll();
                    Quark.LANGUAGE.sendMessageTo(sender, "module", "enable_all");
                }
                case "disable_all" -> {
                    ModuleManager.disableAll();
                    Quark.LANGUAGE.sendMessageTo(sender, "module", "disable_all");
                }
                case "reload_all" -> {
                    ModuleManager.reloadAll();
                    Quark.LANGUAGE.sendMessageTo(sender, "module", "reload_all");
                }
                case "enable" -> {
                    if (ModuleManager.getModuleStatus(args[1]) == ModuleStatus.UNREGISTERED) {
                        this.sendExceptionMessage(sender);
                        return;
                    }
                    ModuleManager.enable(args[1]);
                    ModuleManager.reloadStatus();
                    Quark.LANGUAGE.sendMessageTo(sender, "module", "enable", args[1]);
                }
                case "disable" -> {
                    if (ModuleManager.getModuleStatus(args[1]) == ModuleStatus.UNREGISTERED) {
                        this.sendExceptionMessage(sender);
                        return;
                    }
                    ModuleManager.disable(args[1]);
                    ModuleManager.reloadStatus();
                    Quark.LANGUAGE.sendMessageTo(sender, "module", "disable", args[1]);
                }
                case "reload" -> {
                    if (ModuleManager.getModuleStatus(args[1]) == ModuleStatus.UNREGISTERED) {
                        this.sendExceptionMessage(sender);
                        return;
                    }
                    ModuleManager.reload(args[1]);
                    Quark.LANGUAGE.sendMessageTo(sender, "module", "reload", args[1]);
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
            if (args.length == 1) {
                tabList.add("list_all");
                tabList.add("enable");
                tabList.add("disable");
                tabList.add("reload");
                tabList.add("enable_all");
                tabList.add("disable_all");
                tabList.add("reload_all");
                return;
            }
            if (args.length == 2 && !args[0].contains("_all")) {
                tabList.addAll(ModuleManager.MODULES.keySet().stream().sorted().toList());
            }
        }
    }

    @QuarkCommand(name = "password")
    final class SetPasswordCommand extends CoreCommand {
        public static final List<String> MODE_TAB_LIST = List.of("set", "reset");

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            switch (args[0]) {
                case "set" -> SharedContext.SHARED_THREAD_POOL.submit(() -> {
                    PlayerAuthService.set(sender.getName(), args[1]);
                    Quark.LANGUAGE.sendMessageTo(sender, "auth", "password_set", args[1]);
                });
                case "reset" -> SharedContext.SHARED_THREAD_POOL.submit(() -> {
                    String rand = PlayerAuthService.generateRandom();
                    PlayerAuthService.set(sender.getName(), rand);
                    Quark.LANGUAGE.sendMessageTo(sender, "auth", "password_set", rand);
                });
                default -> this.sendExceptionMessage(sender);
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
            switch (args.length) {
                case 1 -> tabList.addAll(MODE_TAB_LIST);
                case 2 -> {
                    if (Objects.equals(args[0], "set")) tabList.add("<password>");
                }
            }
        }
    }
}
