package org.atcraftmc.starlight.framework.module;

import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.apm.remote.event.MessengerEventChannel;
import me.gb2022.apm.remote.event.RemoteEventListener;
import me.gb2022.commons.reflect.Annotations;
import me.gb2022.commons.reflect.AutoRegisterManager;
import me.gb2022.commons.reflect.DependencyInjector;
import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.pack.IPackage;
import me.gb2022.modular.subcomponent.ComponentProvider;
import me.gb2022.modular.subcomponent.SubComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.command.AbstractCommand;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.core.JDBCService;
import org.atcraftmc.starlight.core.RemoteMessageService;
import org.atcraftmc.starlight.core.data.BanEntryService;
import org.atcraftmc.starlight.core.data.FlexibleMapService;
import org.atcraftmc.starlight.core.data.RegionService;
import org.atcraftmc.starlight.core.data.WaypointService;
import org.atcraftmc.starlight.core.permission.PermissionService;
import org.atcraftmc.starlight.data.assets.Asset;
import org.atcraftmc.starlight.data.assets.AssetGroup;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.StarlightCommandManager;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;


public interface ModuleServices {
    Logger LOGGER = LogManager.getLogger("SLModuleServices");

    ModuleAutoRegManager AUTO_REG = new ModuleAutoRegManager();
    DependencyInjector<SLModule> MODULE_DEPENDENCY_INJECTOR = new ModuleDependencyInjector();

    static void onEnable(SLModuleHandle module) {
        module.getComponents().clear();
        for (var component : createComponents(module.getModule().orElseThrow())) {
            module.getComponents().put((Class<? extends SLModuleComponent<?>>) component.getClass(), (component));
        }

        MODULE_DEPENDENCY_INJECTOR.inject(module.getModule(SLModule.class).orElseThrow());
        AUTO_REG.attach(module);
        initCommands(module);
    }

    static void onDisable(SLModuleHandle module) {
        AUTO_REG.detach(module);

        if (module.getClass().getDeclaredAnnotation(CommandProvider.class) != null) {
            for (AbstractCommand cmd : module.getCommands()) {
                Starlight.instance().getCommandManager().unregister(cmd);
            }
        }

        module.getComponents().clear();
    }

    static <E> Set<SubComponent<E>> createComponents(E holder) {
        var components = new HashSet<SubComponent<E>>();

        Annotations.matchAnnotation(holder, ComponentProvider.class, (a) -> {
            for (var clazz : a.value()) {
                SubComponent<E> component;

                try {
                    component = (SubComponent<E>) clazz.getDeclaredConstructor().newInstance();
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

                component.ctx(holder);

                components.add(component);
            }
        });

        return components;
    }

    @SuppressWarnings({"rawtypes"})
    static void initCommands(SLModuleHandle handle) {
        var module = handle.getModule().orElseThrow();

        if (!module.getClass().isAnnotationPresent(CommandProvider.class)) {
            return;
        }

        handle.getCommands().clear();

        var annotation = module.getClass().getAnnotation(CommandProvider.class);

        for (Class<? extends AbstractCommand> commandClass : annotation.value()) {
            AbstractCommand cmd;
            try {
                cmd = commandClass.getConstructor().newInstance();
                if (cmd instanceof ModuleCommand c) {
                    c.initContext(handle.getModule(SLModule.class).orElseThrow());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            StarlightCommandManager.getInstance().register(cmd);
            handle.getCommands().add(cmd);
        }
    }

    final class ModuleDependencyInjector extends DependencyInjector<SLModule> {
        public ModuleDependencyInjector() {
            Function<String[], Boolean> useCacheForAsset = (p) -> p.length == 1 || Boolean.parseBoolean(p[1]);

            registerInjector(Asset.class, (p, m) -> new Asset(m.ownerPlugin(), p[0], useCacheForAsset.apply(p)));
            registerInjector(AssetGroup.class, (p, m) -> new AssetGroup(m.ownerPlugin(), p[0], useCacheForAsset.apply(p)));
            registerInjector(Permission.class, (p, m) -> PermissionService.createPermissionObject(p[0]));

            registerInjector(IPackage.class, (p, m) -> m.parent());
            registerInjector(Plugin.class, (p, m) -> m.ownerPlugin());

            registerInjector(Logger.class, (p, m) -> m.handle().getLogger());
            registerInjector(LanguageEntry.class, (p, m) -> m.handle().getLanguage());
            registerInjector(LanguageItem.class, (p, m) -> Starlight.lang().item(m.parent().getId(), m.id(), p[0]));

            registerInjector(RegionService.class, (a, m) -> {
                var service = new RegionService(a[1]);
                try {
                    service.init(JDBCService.getDB(a[0]).orElseThrow());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return service;
            });
            registerInjector(WaypointService.class, (a, m) -> {
                var service = new WaypointService(a[1]);
                try {
                    service.init(JDBCService.getDB(a[0]).orElseThrow());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return service;
            });
            registerInjector(BanEntryService.class, (a, m) -> {
                var service = new BanEntryService(a[1]);
                try {
                    service.init(JDBCService.getDB(a[0]).orElseThrow());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return service;
            });
            registerInjector(FlexibleMapService.class, (a, m) -> {
                var service = new FlexibleMapService(a[1]);
                try {
                    service.init(JDBCService.getDB(a[0]).orElseThrow());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return service;
            });
        }

        @Override
        public <T> T createInjection(Class<T> type, SLModule owner, String argument) {
            return super.createInjection(type, owner, argument.replace("/", ";"));
        }
    }

    final class ModuleAutoRegManager extends AutoRegisterManager<Listener> {
        public ModuleAutoRegManager() {
            Builder.build(this, (i) -> {
                i.attach(Registrations.SERVER_EVENT, BukkitUtil::registerEventListener);
                i.detach(Registrations.SERVER_EVENT, BukkitUtil::unregisterEventListener);
                i.attach(Registrations.PLUGIN_MESSAGE, PluginMessenger.EVENT_BUS::registerEventListener);
                i.detach(Registrations.PLUGIN_MESSAGE, PluginMessenger.EVENT_BUS::unregisterEventListener);
                i.attach(Registrations.PLUGIN_VPN_EVENT, Builder.apmService((l, s) -> s.registerEventHandler(l)));
                i.detach(Registrations.PLUGIN_VPN_EVENT, Builder.apmService((l, s) -> s.removeMessageHandler(l)));
                i.attach(Registrations.PLUGIN_VPN_LISTENER, Builder.apmEvent((l, s) -> s.addListener(l)));
                i.detach(Registrations.PLUGIN_VPN_LISTENER, Builder.apmEvent((l, s) -> s.removeListener(l)));
                i.attach(Registrations.CLIENT_MESSAGE, (l) -> System.out.println("deprecated register: client message API"));
                i.detach(Registrations.CLIENT_MESSAGE, (l) -> System.out.println("deprecated register: client message API"));
            });
        }

        public void attach(SLModuleHandle object) {
            this.attach(object.getModule(SLModule.class).orElseThrow());

            for (var component : object.getComponents().values()) {
                this.attach((Listener) component);
            }
        }

        public void detach(SLModuleHandle object) {
            this.detach(object.getModule(SLModule.class).orElseThrow());

            for (var component : object.getComponents().values()) {
                this.detach((Listener) component);
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

        private static final class Builder {
            private final Map<String, Consumer<Listener>> attachFunctions = new HashMap<>();
            private final Map<String, Consumer<Listener>> detachFunctions = new HashMap<>();

            static void build(AutoRegisterManager<Listener> target, Consumer<Builder> func) {
                var builder = new Builder();
                func.accept(builder);
                builder.build(target);
            }

            public static Consumer<Listener> apmService(BiConsumer<Listener, RemoteMessageService> func) {
                return listener -> func.accept(listener, RemoteMessageService.instance());
            }

            public static Consumer<Listener> apmEvent(BiConsumer<RemoteEventListener, MessengerEventChannel> func) {
                return listener -> func.accept((RemoteEventListener) listener, RemoteMessageService.instance().eventChannel());
            }

            public void attach(String id, Consumer<Listener> function) {
                attachFunctions.put(id, function);
            }

            public void detach(String id, Consumer<Listener> function) {
                detachFunctions.put(id, function);
            }

            public void build(AutoRegisterManager<Listener> target) {
                for (var s : attachFunctions.keySet()) {
                    target.registerHandler(s, this.attachFunctions.get(s), this.detachFunctions.get(s));
                }
            }
        }
    }
}
