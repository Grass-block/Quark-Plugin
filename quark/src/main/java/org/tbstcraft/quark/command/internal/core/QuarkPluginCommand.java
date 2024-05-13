package org.tbstcraft.quark.command.internal.core;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.ProductInfo;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.command.CommandManager;
import org.tbstcraft.quark.command.CoreCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.framework.config.Language;
import org.tbstcraft.quark.util.api.APIProfileTest;
import org.tbstcraft.quark.util.api.BukkitPluginManager;
import org.tbstcraft.quark.util.container.ObjectContainer;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@QuarkCommand(name = "quark", permission = "+quark.command", subCommands = {
        ConfigCommand.class,
        LanguageCommand.class,
        ModuleCommand.class,
        GlobalVarsCommand.class,
        PackageCommand.class,
        QuarkPluginCommand.ReloadCommand.class
        //DataUpdateCommand.class
})
public final class QuarkPluginCommand extends CoreCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "info" -> ProductInfo.sendInfoDisplay(sender);
            case "sync-commands" -> {
                CommandManager.syncCommands();
                Quark.LANGUAGE.sendMessageTo(sender, "command", "sync-commands");
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
                String locale = Language.getLocale(sender);
                this.message = Quark.LANGUAGE.getMessage(locale, "packages", "load");
            }

            public static void initLoaders() {
                try {
                    PACKAGE_LOADER_CLASS.set(Class.forName("org.tbstcraft.quark.framework.packages.PackageManager"));
                    PLUGIN_LOADER_CLASS.set(Class.forName("org.tbstcraft.quark.util.api.BukkitPluginManager"));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            private static void reload(CommandSender sender) {
                new ReloadTask(sender).run();
            }

            @Override
            public void run() {
                if(APIProfileTest.isArclightBasedServer()){
                    throw new UnsupportedOperationException("RELOADING IS UNSUPPORTED ON ARCLIGHT BASED PLATFORM!");
                }
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
