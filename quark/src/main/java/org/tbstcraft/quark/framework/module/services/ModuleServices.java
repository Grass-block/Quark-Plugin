package org.tbstcraft.quark.framework.module.services;

import me.gb2022.apm.client.ClientMessenger;
import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.reflect.Annotations;
import org.tbstcraft.quark.command.AbstractCommand;
import org.tbstcraft.quark.command.CommandManager;
import org.tbstcraft.quark.command.CommandRegistry;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.module.compat.Compat;
import org.tbstcraft.quark.framework.module.compat.CompatContainer;
import org.tbstcraft.quark.framework.module.compat.CompatDelegate;
import org.tbstcraft.quark.service.network.RemoteMessageService;
import org.tbstcraft.quark.util.api.APIProfile;
import org.tbstcraft.quark.util.api.APIProfileTest;
import org.tbstcraft.quark.util.api.BukkitUtil;

import java.lang.reflect.Constructor;

public interface ModuleServices {
    static void init(AbstractModule module) {
        initCompatContainers(module);
        initCommands(module);
        if(Annotations.hasAnnotation(module, EventListener.class)){
            BukkitUtil.registerEventListener(module);
            for (CompatContainer<?> container : module.getCompatContainers().values()) {
                BukkitUtil.registerEventListener(container);
            }
        }

        if(Annotations.hasAnnotation(module, PluginMessageListener.class)){
            PluginMessenger.EVENT_BUS.registerEventListener(module);
        }
        if(Annotations.hasAnnotation(module, ClientMessageListener.class)){
            ClientMessenger.EVENT_BUS.registerEventListener(module);
        }
        if(Annotations.hasAnnotation(module, RemoteMessageListener.class)){
            RemoteMessageService.getInstance().addMessageHandler(module);
        }
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
        CommandRegistry annotation = module.getClass().getAnnotation(CommandRegistry.class);
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
        if (module.getClass().getDeclaredAnnotation(EventListener.class) != null) {
            BukkitUtil.unregisterEventListener(module);
            for (CompatContainer<?> container : module.getCompatContainers().values()) {
                BukkitUtil.unregisterEventListener(container);
            }
        }
        if (module.getClass().getDeclaredAnnotation(CommandRegistry.class) != null) {
            for (AbstractCommand cmd : module.getCommands()) {
                CommandManager.unregisterCommand(cmd);
            }
        }

        if(Annotations.hasAnnotation(module, PluginMessageListener.class)){
            PluginMessenger.EVENT_BUS.unregisterEventListener(module);
        }
        if(Annotations.hasAnnotation(module, ClientMessageListener.class)){
            ClientMessenger.EVENT_BUS.unregisterEventListener(module);
        }
        if(Annotations.hasAnnotation(module, RemoteMessageListener.class)){
            RemoteMessageService.getInstance().removeMessageHandler(module);
        }
    }
}
