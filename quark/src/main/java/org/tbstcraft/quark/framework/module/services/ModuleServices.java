package org.tbstcraft.quark.framework.module.services;

import me.gb2022.apm.client.ClientMessenger;
import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.reflect.Annotations;
import me.gb2022.commons.reflect.AutoRegisterManager;
import me.gb2022.commons.reflect.DependencyInjector;
import org.atcraftmc.qlib.command.AbstractCommand;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.assets.Asset;
import org.tbstcraft.quark.data.assets.AssetGroup;
import org.atcraftmc.qlib.language.LanguageContainer;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommandManager;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.framework.FunctionalComponent;
import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.module.component.Components;
import org.tbstcraft.quark.framework.module.component.ModuleComponent;
import org.tbstcraft.quark.framework.packages.IPackage;
import org.tbstcraft.quark.framework.record.RecordEntry;
import org.tbstcraft.quark.framework.record.RecordService;
import org.tbstcraft.quark.internal.RemoteMessageService;
import org.tbstcraft.quark.internal.permission.PermissionService;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;

@SuppressWarnings({"rawtypes"})
public interface ModuleServices {
    ModuleAutoRegManager AUTO_REG = new ModuleAutoRegManager();
    DependencyInjector<AbstractModule> MODULE_DEPENDENCY_INJECTOR = new ModuleDependencyInjector();

    static void onEnable(AbstractModule module) {
        module.getComponents().clear();
        for (ModuleComponent<?> component : createComponents(module)) {
            module.getComponents().put((Class<? extends ModuleComponent<?>>) component.getClass(), component);
        }

        MODULE_DEPENDENCY_INJECTOR.inject(module);

        initCommands(module);

        AUTO_REG.attach(module);
    }

    static void onDisable(AbstractModule module) {
        AUTO_REG.detach(module);

        for (FunctionalComponent component : module.getComponents().values()) {
            AUTO_REG.detach(component);
        }

        module.getComponents().clear();

        if (module.getClass().getDeclaredAnnotation(CommandProvider.class) != null) {
            for (AbstractCommand cmd : module.getCommands()) {
                Quark.getInstance().getCommandManager().unregister(cmd);
            }
        }
    }

    static <E extends AbstractModule> Set<ModuleComponent<E>> createComponents(E module) {
        Set<ModuleComponent<E>> components = new HashSet<>();

        Annotations.matchAnnotation(module, Components.class, (a) -> {
            for (Class<? extends ModuleComponent<?>> clazz : a.value()) {
                ModuleComponent<E> component;

                try {
                    component = (ModuleComponent<E>) clazz.getDeclaredConstructor().newInstance();
                } catch (NoClassDefFoundError ignored) {
                    continue;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }

                try {
                    component.checkCompatibility();
                } catch (APIIncompatibleException ignored) {
                    continue;
                }

                component.ctx(module);

                components.add(component);
            }
        });

        return components;
    }

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
            QuarkCommandManager.getInstance().register(cmd);
            module.getCommands().add(cmd);
        }
    }


    class ModuleDependencyInjector extends DependencyInjector<AbstractModule> {
        public ModuleDependencyInjector() {
            Function<String[], Boolean> useCacheForAsset = (p) -> p.length == 1 || Boolean.parseBoolean(p[1]);

            registerInjector(Asset.class, (p, m) -> new Asset(m.getOwnerPlugin(), p[0], useCacheForAsset.apply(p)));
            registerInjector(AssetGroup.class, (p, m) -> new AssetGroup(m.getOwnerPlugin(), p[0], useCacheForAsset.apply(p)));
            registerInjector(Permission.class, (p, m) -> PermissionService.createPermissionObject(p[0]));
            registerInjector(LanguageEntry.class, (p, m) -> m.getLanguage());
            registerInjector(LanguageItem.class, (p, m) -> LanguageContainer.getInstance().item(m.getParent().getId(), m.getId(), p[0]));
            registerInjector(IPackage.class, (p, m) -> m.getParent());
            registerInjector(Plugin.class, (p, m) -> m.getOwnerPlugin());
            registerInjector(Logger.class, (p, m) -> m.getLogger());
            registerInjector(org.apache.logging.log4j.Logger.class, (p, m) -> m.getL4jLogger());
            registerInjector(RecordEntry.class, (p, m) -> {
                var id = m.getId();
                var format = m.getRecordFormat();

                if (p.length == 1) {
                    format = p[0].split(",");
                }
                if (p.length == 2) {
                    id = p[0];
                    format = p[1].split(",");
                }

                return RecordService.create(id, format);
            });
        }
    }

    class ModuleAutoRegManager extends AutoRegisterManager<Listener> {
        public ModuleAutoRegManager() {
            this.registerHandler(ServiceType.EVENT_LISTEN, BukkitUtil::registerEventListener, BukkitUtil::unregisterEventListener);
            this.registerHandler(
                    ServiceType.CLIENT_MESSAGE,
                    ClientMessenger.EVENT_BUS::registerEventListener,
                    ClientMessenger.EVENT_BUS::unregisterEventListener
                                );
            this.registerHandler(
                    ServiceType.PLUGIN_MESSAGE,
                    PluginMessenger.EVENT_BUS::registerEventListener,
                    PluginMessenger.EVENT_BUS::unregisterEventListener
                                );
            this.registerHandler(ServiceType.REMOTE_MESSAGE, RemoteMessageService::addHandler, RemoteMessageService::removeHandler);
        }

        public void attach(AbstractModule object) {
            this.attach(((Listener) object));

            for (FunctionalComponent component : object.getComponents().values()) {
                this.attach(component);
            }
        }

        public void detach(AbstractModule object) {
            this.detach(((Listener) object));

            for (FunctionalComponent component : object.getComponents().values()) {
                this.detach(component);
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
