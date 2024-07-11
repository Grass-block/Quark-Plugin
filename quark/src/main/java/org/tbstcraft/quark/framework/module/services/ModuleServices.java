package org.tbstcraft.quark.framework.module.services;

import me.gb2022.apm.client.ClientMessenger;
import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.reflect.AutoRegisterManager;
import me.gb2022.commons.reflect.DependencyInjector;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.tbstcraft.quark.data.assets.Asset;
import org.tbstcraft.quark.data.assets.AssetGroup;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.AbstractCommand;
import org.tbstcraft.quark.foundation.command.CommandManager;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.platform.APIProfile;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.module.compat.Compat;
import org.tbstcraft.quark.framework.module.compat.CompatContainer;
import org.tbstcraft.quark.framework.module.compat.CompatDelegate;
import org.tbstcraft.quark.internal.RemoteMessageService;
import org.tbstcraft.quark.internal.permission.PermissionService;

import java.lang.reflect.Constructor;

public interface ModuleServices {
    ModuleAutoRegManager AUTO_REG = new ModuleAutoRegManager();
    DependencyInjector<AbstractModule> MODULE_DEPENDENCY_INJECTOR = DependencyInjector.<AbstractModule>builder()
            .injector(Asset.class, (p, m) -> new Asset(m.getOwnerPlugin(), p[0], p.length == 1 || Boolean.parseBoolean(p[1])))//true as default
            .injector(AssetGroup.class, (p, m) -> new AssetGroup(m.getOwnerPlugin(), p[0], p.length == 1 || Boolean.parseBoolean(p[1])))
            .injector(Permission.class, (p, m) -> PermissionService.createPermissionObject(p[0]))
            .injector(LanguageEntry.class, (p, m) -> m.getLanguage())
            .injector(LanguageItem.class, (p, m) -> m.getParent().getLanguageFile().item(m.getId(), p[0]))
            .build();

    static void init(AbstractModule module) {
        MODULE_DEPENDENCY_INJECTOR.inject(module);

        initCompatContainers(module);
        initCommands(module);

        AUTO_REG.attach(module);
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
        AUTO_REG.detach(module);

        if (module.getClass().getDeclaredAnnotation(CommandProvider.class) != null) {
            for (AbstractCommand cmd : module.getCommands()) {
                CommandManager.unregisterCommand(cmd);
            }
        }
    }

    class ModuleAutoRegManager extends AutoRegisterManager<Listener> {
        public ModuleAutoRegManager() {
            this.registerHandler(ServiceType.EVENT_LISTEN, BukkitUtil::registerEventListener, BukkitUtil::unregisterEventListener);
            this.registerHandler(ServiceType.CLIENT_MESSAGE, ClientMessenger.EVENT_BUS::registerEventListener, ClientMessenger.EVENT_BUS::unregisterEventListener);
            this.registerHandler(ServiceType.PLUGIN_MESSAGE, PluginMessenger.EVENT_BUS::registerEventListener, PluginMessenger.EVENT_BUS::unregisterEventListener);
            this.registerHandler(ServiceType.REMOTE_MESSAGE, RemoteMessageService::addHandler, RemoteMessageService::removeHandler);
        }

        public void attach(AbstractModule object) {
            this.attach(((Listener) object));
            for (CompatContainer<?> container : object.getCompatContainers().values()) {
                this.attach(container);
            }
        }

        public void detach(AbstractModule object) {
            this.detach(((Listener) object));
            for (CompatContainer<?> container : object.getCompatContainers().values()) {
                this.detach(container);
            }
        }

        @Override
        public void handleAttachFailed(Listener object, String type) {
            System.out.println("no module service named " + type);
        }

        @Override
        public void handleDetachFailed(Listener object, String type) {
            System.out.println("no module service named " + type);
        }
    }
}
