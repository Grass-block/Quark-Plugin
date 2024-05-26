package org.tbstcraft.quark.framework.module.services;

import me.gb2022.apm.client.ClientMessenger;
import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.reflect.Annotations;
import org.bukkit.event.Listener;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.command.AbstractCommand;
import org.tbstcraft.quark.framework.command.CommandManager;
import org.tbstcraft.quark.framework.command.CommandProvider;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.module.compat.Compat;
import org.tbstcraft.quark.framework.module.compat.CompatContainer;
import org.tbstcraft.quark.framework.module.compat.CompatDelegate;
import org.tbstcraft.quark.service.network.RemoteMessageService;
import org.tbstcraft.quark.util.platform.APIProfile;
import org.tbstcraft.quark.util.platform.APIProfileTest;
import org.tbstcraft.quark.util.platform.BukkitUtil;

import java.lang.reflect.Constructor;

public interface ModuleServices {
    static void set(Listener module, String service) {
        switch (service) {
            case ServiceType.EVENT_LISTEN -> BukkitUtil.registerEventListener(module);
            case ServiceType.PLUGIN_MESSAGE -> PluginMessenger.EVENT_BUS.registerEventListener(module);
            case ServiceType.REMOTE_MESSAGE -> RemoteMessageService.getInstance().addMessageHandler(module);
            case ServiceType.CLIENT_MESSAGE -> ClientMessenger.EVENT_BUS.registerEventListener(module);
            default -> Quark.LOGGER.warning("no module service named " + service);
        }
    }

    static void unset(Listener module, String service) {
        switch (service) {
            case ServiceType.EVENT_LISTEN -> BukkitUtil.unregisterEventListener(module);
            case ServiceType.PLUGIN_MESSAGE -> PluginMessenger.EVENT_BUS.unregisterEventListener(module);
            case ServiceType.REMOTE_MESSAGE -> RemoteMessageService.getInstance().removeMessageHandler(module);
            case ServiceType.CLIENT_MESSAGE -> ClientMessenger.EVENT_BUS.unregisterEventListener(module);
            default -> Quark.LOGGER.warning("no module service named " + service);
        }
    }


    static void init(AbstractModule module) {
        initCompatContainers(module);
        initCommands(module);

        Annotations.matchAnnotation(module, ModuleService.class, (a) -> {
            for (String service : a.value()) {
                set(module, service);

                for (CompatContainer<?> container : module.getCompatContainers().values()) {
                    set(container, service);
                }
            }
        });
    }

    static void initCompatContainers(AbstractModule module) {
        Compat annotation = module.getClass().getAnnotation(Compat.class);
        if (annotation == null) {
            return;
        }
        module.getCompatContainers().clear();
        for (Class<? extends CompatContainer<?>> compatClazz : annotation.value()) {
            CompatDelegate delegate = compatClazz.getAnnotation(CompatDelegate.class);
            if (delegate == null) {
                continue;
            }

            boolean valid = false;

            for (APIProfile profile : delegate.value()) {
                if (APIProfileTest.getAPIProfile().isCompat(profile)) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                continue;
            }

            try {
                Constructor<? extends CompatContainer<?>> constructor = compatClazz.getConstructor(module.getClass());
                CompatContainer<?> container = constructor.newInstance(module);
                module.getCompatContainers().put(compatClazz, container);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void initCommands(AbstractModule module) {
        CommandProvider annotation = module.getClass().getAnnotation(CommandProvider.class);
        if (annotation == null) {
            return;
        }

        module.getCommands().clear();
        for (Class<? extends AbstractCommand> commandClass : annotation.value()) {
            AbstractCommand cmd;
            try {
                cmd = commandClass.getConstructor().newInstance();
                if (cmd instanceof ModuleCommand c) {
                    c.initContext(module);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            CommandManager.registerCommand(cmd);
            module.getCommands().add(cmd);
        }
    }

    static void disable(AbstractModule module) {
        Annotations.matchAnnotation(module, ModuleService.class, (a) -> {
            for (String service : a.value()) {
                unset(module, service);
                for (CompatContainer<?> container : module.getCompatContainers().values()) {
                    unset(container, service);
                }
            }
        });

        if (module.getClass().getDeclaredAnnotation(CommandProvider.class) != null) {
            for (AbstractCommand cmd : module.getCommands()) {
                CommandManager.unregisterCommand(cmd);
            }
        }
    }
}
