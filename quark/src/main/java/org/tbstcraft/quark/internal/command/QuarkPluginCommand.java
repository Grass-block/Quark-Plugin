package org.tbstcraft.quark.internal.command;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.ProductInfo;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.command.CommandManager;
import org.tbstcraft.quark.framework.command.CoreCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.data.language.Language;
import org.tbstcraft.quark.util.container.ObjectContainer;
import org.tbstcraft.quark.util.platform.APIProfileTest;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;

@QuarkCommand(name = "quark", permission = "+quark.command", subCommands = {
        ConfigCommand.class,
        LanguageCommand.class,
        ModuleCommand.class,
        GlobalVarsCommand.class,
        PackageCommand.class,
        QuarkPluginCommand.ReloadCommand.class
})
public final class QuarkPluginCommand extends CoreCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "info" -> ProductInfo.sendInfoDisplay(sender);
            case "stats" -> ProductInfo.sendStatsDisplay(sender);
            case "sync-commands" -> {
                CommandManager.syncCommands();
                Quark.LANGUAGE.sendMessage(sender, "command", "sync-commands");
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("info");
            tabList.add("sync-commands");
            tabList.add("stats");
        }
    }

    @QuarkCommand(name = "reload", permission = "-quark.configure.reload")
    public static final class ReloadCommand extends CoreCommand {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (APIProfileTest.isArclightBasedServer()) {
                this.getLanguage().sendMessage(sender, "platform-unsupported");
                return;

            }
            if (Quark.PLUGIN.isFastBoot()) {
                this.getLanguage().sendMessage(sender, "fastboot-unsupported");
                return;
            }

            this.fullyReload(sender);
        }

        private void fullyReload(CommandSender sender) {
            new ReloadTask(sender).run();
        }

        public static class ReloadTask implements Runnable {
            private static final ObjectContainer<Class<?>> PLUGIN_LOADER_CLASS = new ObjectContainer<>();
            private static final ObjectContainer<Class<?>> PACKAGE_LOADER_CLASS = new ObjectContainer<>();

            private final String message;
            private final CommandSender sender;

            private ReloadTask(CommandSender sender) {
                this.sender = sender;
                Locale locale = Language.locale(sender);
                this.message = Quark.LANGUAGE.getMessage(locale, "packages", "load");
            }

            public static void initLoaders() {
                try {
                    PACKAGE_LOADER_CLASS.set(Class.forName("org.tbstcraft.quark.framework.packages.PackageManager"));
                    PLUGIN_LOADER_CLASS.set(Class.forName("org.tbstcraft.quark.util.platform.BukkitPluginManager"));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void run() {
                try {
                    PLUGIN_LOADER_CLASS.get().getMethod("reload", String.class).invoke(null, Quark.PLUGIN_ID);
                    PACKAGE_LOADER_CLASS.get().getMethod("loadSubPacks").invoke(null);
                    CommandManager.syncCommands();
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }

                this.sender.sendMessage(this.message);
            }
        }
    }
}
