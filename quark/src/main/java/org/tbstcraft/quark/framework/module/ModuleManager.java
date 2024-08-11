package org.tbstcraft.quark.framework.module;

import me.gb2022.commons.TriState;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.platform.APIProfile;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceHolder;
import org.tbstcraft.quark.framework.service.ServiceInject;
import org.tbstcraft.quark.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

@QuarkService(id = "module")
public interface ModuleManager extends Service {
    String DATA_FILE = "%s/data/modules.properties";

    ServiceHolder<ModuleManager> INSTANCE = new ServiceHolder<>();

    static ModuleManager getInstance() {
        return ModuleManager.INSTANCE.get();
    }

    @ServiceInject
    static void start() {
        INSTANCE.set(new Impl(Quark.PLUGIN));
        INSTANCE.get().onEnable();
    }

    @ServiceInject
    static void stop() {
        INSTANCE.get().onDisable();
    }

    static ModuleManager create(Plugin p) {
        return new Impl(p);
    }

    static AbstractModule getModule(String id) {
        return INSTANCE.get().get(id);
    }

    //operation
    static ObjectOperationResult enableModule(String id) {
        return INSTANCE.get().enable(id);
    }

    static ObjectOperationResult disableModule(String id) {
        return INSTANCE.get().disable(id);
    }

    static ObjectOperationResult reloadModule(String id) {
        return INSTANCE.get().reload(id);
    }

    static void enableAllModules() {
        INSTANCE.get().enableAll();
    }

    static void disableAllModules() {
        INSTANCE.get().disableAll();
    }

    static void reloadAllModules() {
        INSTANCE.get().reloadAll();
    }

    static Set<AbstractModule> getByStatus(TriState status) {
        Set<AbstractModule> result = new HashSet<>();
        for (String id : INSTANCE.get().getModules().keySet()) {
            if (getModuleStatus(id) != status) {
                continue;
            }
            result.add(getModule(id));
        }
        return result;
    }

    static Set<String> getIdsByStatus(TriState status) {
        Set<String> result = new HashSet<>();
        for (String id : INSTANCE.get().getModules().keySet()) {
            if (getModuleStatus(id) != status) {
                continue;
            }
            result.add(id);
        }
        return result;
    }


    //register
    static void registerModule(PackageModule m, Logger logger) {
        INSTANCE.get().register(m, logger);
    }

    static void unregisterModule(String id, Logger logger) {
        INSTANCE.get().unregister(id, logger);
    }

    //status
    static boolean isEnabled(String id) {
        return getModuleStatus(id) == TriState.FALSE;
    }

    static TriState getModuleStatus(String id) {
        return INSTANCE.get().getStatus(id);
    }

    static Map<String, AbstractModule> getAllModules() {
        return INSTANCE.get().getModules();
    }

    //query
    AbstractModule get(String id);

    Map<String, AbstractModule> getModules();

    TriState getStatus(String id);


    //operation
    ObjectOperationResult enable(String id);

    ObjectOperationResult disable(String id);

    default ObjectOperationResult reload(String id) {
        ObjectOperationResult result = this.disable(id);
        if (result != ObjectOperationResult.SUCCESS) {
            return result;
        }
        return enable(id);
    }

    default void enableAll() {
        for (String id : this.getModules().keySet()) {
            this.enable(id);
        }
    }

    default void disableAll() {
        for (String id : this.getModules().keySet()) {
            this.disable(id);
        }
    }

    default void reloadAll() {
        List<String> list = new ArrayList<>();
        for (String id : this.getModules().keySet()) {
            if (this.disable(id) != ObjectOperationResult.SUCCESS) {
                continue;
            }
            list.add(id);
        }
        for (String s : list) {
            this.enable(s);
        }
    }


    //register
    void register(AbstractModule m, Logger callback);

    void unregister(String id, Logger callback);

    Set<String> getRegisterFailed();

    class Impl implements ModuleManager {
        private final String parentName;
        private final Logger logger;
        private final Map<String, AbstractModule> moduleMap = new HashMap<>();
        private final Properties statusMap = new Properties();
        private final Set<String> registerFailed = new HashSet<>();
        private final Map<String, AbstractModule> internals = new HashMap<>();

        public Impl(Plugin parent) {
            this.parentName = parent.getName();
            this.logger = parent.getLogger();
        }

        @Override
        public void onEnable() {
            try {
                DataFix.moveFile("/config/modules.properties", "/data/modules.properties");
                this.statusMap.load(new FileInputStream(this.getStatusFile()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onDisable() {
            this.saveStatus();
            for (String id : new ArrayList<>(this.getModules().keySet())) {
                this.unregister(id, Quark.LOGGER);
            }
        }

        private void saveStatus() {
            try {
                this.statusMap.store(new FileOutputStream(this.getStatusFile()), "auto generated file,please don't edit it.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private File getStatusFile() {
            String path = DATA_FILE.formatted(FilePath.pluginFolder(this.parentName));
            File file = new File(path);
            if (!file.exists() || file.length() == 0) {
                if (file.getParentFile().mkdirs()) {
                    this.logger.info("created module status file folder.");
                }
                try {
                    if (file.createNewFile()) {
                        this.logger.info("created module status file.");
                    }
                } catch (IOException e) {
                    this.logger.severe("failed to create status file");
                    return file;
                }
                return file;
            }
            return file;
        }

        private ObjectOperationResult enable0(String id) {
            if (getStatus(id) == TriState.UNKNOWN) {
                return ObjectOperationResult.NOT_FOUND;
            }
            if (getStatus(id) == TriState.FALSE) {
                return ObjectOperationResult.ALREADY_OPERATED;
            }
            try {
                this.get(id).enableModule();
            } catch (Exception ex) {
                ExceptionUtil.log(ex);
                return ObjectOperationResult.INTERNAL_ERROR;
            }
            this.statusMap.put(id, "enabled");
            return ObjectOperationResult.SUCCESS;
        }

        private ObjectOperationResult disable0(String id) {
            if (getStatus(id) == TriState.UNKNOWN) {
                return ObjectOperationResult.NOT_FOUND;
            }
            if (getStatus(id) == TriState.TRUE) {
                return ObjectOperationResult.ALREADY_OPERATED;
            }
            try {
                this.get(id).disableModule();
            } catch (Exception ex) {
                ExceptionUtil.log(ex);
                return ObjectOperationResult.INTERNAL_ERROR;
            }
            this.statusMap.put(id, "disabled");
            return ObjectOperationResult.SUCCESS;
        }


        @Override
        public ObjectOperationResult enable(String id) {
            if (get(id).getDescriptor().internal()) {
                return ObjectOperationResult.BLOCKED_INTERNAL;
            }
            ObjectOperationResult result = enable0(id);
            if (result == ObjectOperationResult.SUCCESS) {
                this.logger.info("enabled module %s.".formatted(id));
            }
            this.saveStatus();
            return result;
        }

        @Override
        public ObjectOperationResult disable(String id) {
            if (get(id).getDescriptor().internal()) {
                return ObjectOperationResult.BLOCKED_INTERNAL;
            }
            ObjectOperationResult result = disable0(id);
            if (result == ObjectOperationResult.SUCCESS) {
                this.logger.info("disabled module %s.".formatted(id));
            }
            this.saveStatus();
            return result;
        }

        @Override
        public TriState getStatus(String id) {
            if (!this.statusMap.containsKey(id)) {
                return TriState.UNKNOWN;
            }
            return Objects.equals(this.statusMap.get(id), "enabled") ? TriState.FALSE : TriState.TRUE;
        }


        @Override
        public void register(AbstractModule m, Logger callback) {
            if (m == null) {
                return;
            }

            for (APIProfile profile : m.getCompatBlacklist()) {
                if (APIProfileTest.getAPIProfile() == profile) {
                    this.registerFailed.add(m.getFullId());
                    return;
                }
            }

            try {
                m.checkCompatibility();
            } catch (Throwable e) {
                this.registerFailed.add(m.getFullId());
                return;
            }

            this.moduleMap.put(m.getFullId(), m);
            if (getModuleStatus(m.getFullId()) == TriState.UNKNOWN) {
                boolean status = false;

                if (m.getDescriptor().defaultEnable()) {
                    status = Quark.PLUGIN.getConfig().getBoolean("config.default-status.module");
                }

                this.statusMap.put(m.getFullId(), status ? "enabled" : "disabled");
                this.saveStatus();
            }
            if (m.getDescriptor().internal()) {
                this.statusMap.put(m.getFullId(), "enabled");
            }
            if (getModuleStatus(m.getFullId()) == TriState.FALSE && !m.isBeta()) {
                try {
                    this.get(m.getFullId()).enableModule();
                } catch (Exception ex) {
                    ExceptionUtil.log(ex);
                }
            }
            //callback.info("registered module %s.".formatted(m.locale()));
        }

        @Override
        public void unregister(String id, Logger callback) {
            if (this.getStatus(id) == TriState.FALSE) {
                AbstractModule m = this.get(id);
                if (m != null) {
                    try {
                        m.disableModule();
                    } catch (Exception ex) {
                        ExceptionUtil.log(ex);
                    }
                }
            }
            this.moduleMap.remove(id);
            this.registerFailed.remove(id);
            //callback.info("unregistered module %s.".formatted(id));
        }


        @Override
        public AbstractModule get(String id) {
            return this.moduleMap.get(id);
        }

        @Override
        public Map<String, AbstractModule> getModules() {
            return this.moduleMap;
        }

        @Override
        public Set<String> getRegisterFailed() {
            return registerFailed;
        }
    }
}
