package org.atcraftmc.starlight.framework.module;

import me.gb2022.commons.TriState;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.framework.service.*;
import org.bukkit.plugin.Plugin;
import org.atcraftmc.starlight.api.event.ModuleEvent;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.APIProfile;
import org.atcraftmc.starlight.foundation.platform.APIProfileTest;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.framework.FunctionalComponentStatus;
import org.atcraftmc.starlight.migration.DataFix;
import org.atcraftmc.starlight.util.ExceptionUtil;
import org.atcraftmc.starlight.util.FilePath;
import org.atcraftmc.starlight.util.ObjectOperationResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

@SLService(id = "module", layer = ServiceLayer.FRAMEWORK)
public final class ModuleManager implements Service {
    public static final ServiceHolder<ModuleManager> INSTANCE = new ServiceHolder<>();

    public static final String DATA_FILE = "%s/data/modules.properties";

    private final String parentName;
    private final Logger logger;
    private final Map<String, AbstractModule> moduleMap = new HashMap<>();
    private final Properties statusMap = new Properties();
    private final Map<String, ModuleMeta> metas = new HashMap<>();

    public ModuleManager(Plugin parent) {
        this.parentName = parent.getName();
        this.logger = parent.getLogger();
    }

    public static ModuleManager getInstance() {
        return ModuleManager.INSTANCE.get();
    }

    @ServiceInject
    public static void start() {
        INSTANCE.set(new ModuleManager(Starlight.instance()));
        INSTANCE.get().onEnable();
    }

    @ServiceInject
    public static void stop() {
        INSTANCE.get().onDisable();
    }


    @Override
    public void enable() {
        try {
            DataFix.moveFile("/config/modules.properties", "/data/modules.properties");
            this.statusMap.load(new FileInputStream(this.getStatusFile()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disable() {
        this.saveStatus();
        for (String id : new ArrayList<>(this.getModules().keySet())) {
            this.unregister(id);
        }
    }

    //----[query]----
    public TriState getModuleStatus(String id) {
        return this.getStatus(id);
    }

    public Set<String> getIdsByStatus(TriState status) {
        Set<String> result = new HashSet<>();
        for (String id : this.getKnownModuleMetas().keySet()) {
            if (getModuleStatus(id) != status) {
                continue;
            }
            result.add(id);
        }
        return result;
    }

    public ModuleMeta getMeta(String id) {
        if (!this.metas.containsKey(id)) {
            return ModuleMeta.dummy(id);
        }

        return this.metas.get(id);
    }

    public AbstractModule get(String id) {
        return this.moduleMap.get(id);
    }

    public Map<String, AbstractModule> getModules() {
        return this.moduleMap;
    }

    public Map<String, ModuleMeta> getKnownModuleMetas() {
        return this.metas;
    }

    public TriState getStatus(String id) {
        if (!this.statusMap.containsKey(id)) {
            return TriState.UNKNOWN;
        }
        return Objects.equals(this.statusMap.get(id), "enabled") ? TriState.TRUE : TriState.FALSE;
    }

    private void saveStatus() {
        try {
            this.statusMap.store(new FileOutputStream(this.getStatusFile()), "auto generated file,please don't edit it.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getStatusFile() {
        String path = DATA_FILE.formatted(FilePath.pluginFolder("starlight"));
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


    //----[operation]----
    public void enableAll() {
        for (String id : this.getModules().keySet()) {
            this.enable(id);
        }
    }

    public void disableAll() {
        for (String id : this.getModules().keySet()) {
            this.disable(id);
        }
    }

    public void reloadAll() {
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

    public ObjectOperationResult enable(String id) {
        if (get(id).getDescriptor().internal()) {
            return ObjectOperationResult.BLOCKED_INTERNAL;
        }

        var result = enable0(id);

        if (result == ObjectOperationResult.SUCCESS) {
            this.logger.info("enabled module %s.".formatted(id));
        }

        this.saveStatus();
        return result;
    }

    public ObjectOperationResult disable(String id) {
        if (get(id).getDescriptor().internal()) {
            return ObjectOperationResult.BLOCKED_INTERNAL;
        }

        var result = disable0(id);

        if (result == ObjectOperationResult.SUCCESS) {
            this.logger.info("disabled module %s.".formatted(id));
        }

        this.saveStatus();
        return result;
    }

    public ObjectOperationResult reload(String id) {
        ObjectOperationResult result = this.disable(id);
        if (result != ObjectOperationResult.SUCCESS) {
            return result;
        }
        return enable(id);
    }

    private ObjectOperationResult checkState0(ModuleMeta meta, FunctionalComponentStatus state) {
        if (meta == null || meta.unknown() || meta.status() == FunctionalComponentStatus.UNKNOWN) {
            return ObjectOperationResult.NOT_FOUND;
        }

        if (meta.status() == state) {
            return ObjectOperationResult.ALREADY_OPERATED;
        }

        return ObjectOperationResult.INTERNAL_ERROR;
    }

    private ObjectOperationResult enable0(String id) {
        var meta = this.getMeta(id);
        var result = checkState0(meta, FunctionalComponentStatus.ENABLE);

        if (result != ObjectOperationResult.INTERNAL_ERROR) {
            return result;
        }

        BukkitUtil.callEvent(new ModuleEvent.PreEnable(getMeta(id)));

        try {
            meta.handle().enableModule();
            result = ObjectOperationResult.SUCCESS;
        } catch (Exception ex) {
            ExceptionUtil.log(ex);
        }

        BukkitUtil.callEvent(new ModuleEvent.Enable(getMeta(id), result));

        if (result == ObjectOperationResult.SUCCESS) {
            this.statusMap.put(id, "enabled");
            meta.status(FunctionalComponentStatus.ENABLE);
        }

        return result;
    }

    private ObjectOperationResult disable0(String id) {
        var meta = this.getMeta(id);
        var result = checkState0(meta, FunctionalComponentStatus.DISABLED);

        if (result != ObjectOperationResult.INTERNAL_ERROR) {
            return result;
        }

        BukkitUtil.callEvent(new ModuleEvent.PreEnable(getMeta(id)));

        try {
            meta.handle().disableModule();
            result = ObjectOperationResult.SUCCESS;
        } catch (Exception ex) {
            ExceptionUtil.log(ex);
        }

        BukkitUtil.callEvent(new ModuleEvent.Enable(getMeta(id), result));

        if (result == ObjectOperationResult.SUCCESS) {
            this.statusMap.put(id, "disabled");
            meta.status(FunctionalComponentStatus.DISABLED);
        }

        return result;
    }


    //----[register]----
    private boolean validPlatform(ModuleMeta meta) {
        for (APIProfile profile : meta.compatBlackList()) {
            if (APIProfileTest.getAPIProfile() != profile) {
                continue;
            }
            meta.status(FunctionalComponentStatus.REGISTER_FAILED);
            meta.additional("ERROR_INCOMPATIBLE_PLATFORM");
            return true;
        }

        return false;
    }

    private boolean validFeature(ModuleMeta meta) {
        if (meta.available().load()) {
            return false;
        }

        meta.status(FunctionalComponentStatus.REGISTER_FAILED);
        meta.additional("NOT_IN_CURRENT_PRODUCT_SETTING");
        return true;
    }

    private boolean construct(ModuleMeta meta) {
        var clazz = meta.reference();

        try {
            var m = (PackageModule) clazz.getDeclaredConstructor().newInstance();
            m.init(meta.id(), meta.parent());
            meta.handle(m);
            meta.status(FunctionalComponentStatus.CONSTRUCT);
        } catch (Throwable e) {
            //ExceptionUtil.log(e);
            meta.status(FunctionalComponentStatus.CONSTRUCT_FAILED);
            meta.additional(e.getCause().getClass().getSimpleName() + "[" + e.getCause().getMessage() + "]");
            return true;
        }

        return false;
    }

    private boolean validAPI(ModuleMeta meta) {
        try {
            meta.handle().checkCompatibility();
        } catch (APIIncompatibleException e) {
            meta.status(FunctionalComponentStatus.REGISTER_FAILED);
            meta.additional("ERROR_INCOMPATIBLE_API: " + e.getMessage());
            return true;
        }

        return false;
    }

    public void registerMeta(ModuleMeta meta) {
        this.metas.put(meta.fullId(), meta);

        if (meta.unknown()) {
            return;
        }
        if (validFeature(meta)) {
            return;
        }
        if (validPlatform(meta)) {
            return;
        }
        if (meta.handle() == null && construct(meta)) {
            return;
        }
        if (validAPI(meta)) {
            return;
        }

        var m = meta.handle();

        this.moduleMap.put(m.getFullId(), m);
        meta.status(FunctionalComponentStatus.REGISTER);

        if (getModuleStatus(m.getFullId()) == TriState.UNKNOWN) {
            boolean status = false;

            if (m.getDescriptor().defaultEnable()) {
                status = Starlight.instance().getConfig().getBoolean("config.default-status.module");
            }

            this.statusMap.put(m.getFullId(), status ? "enabled" : "disabled");
        }
        if (m.getDescriptor().internal()) {
            this.statusMap.put(m.getFullId(), "enabled");
        }

        this.saveStatus();
        if (getModuleStatus(m.getFullId()) == TriState.TRUE && !m.isBeta()) {
            try {
                this.get(m.getFullId()).enableModule();
                meta.status(FunctionalComponentStatus.ENABLE);
            } catch (Exception ex) {
                meta.status(FunctionalComponentStatus.ENABLE_FAILED);
                meta.additional(ex.getMessage());
                ExceptionUtil.log(ex);
            }
        }
    }

    public void register(AbstractModule m) {
        if (m == null) {
            return;
        }

        var meta = ModuleMeta.wrap(m);
        this.metas.put(m.getFullId(), meta);

        registerMeta(meta);
    }

    public void unregister(String id) {
        if (this.getStatus(id) == TriState.TRUE) {
            AbstractModule m = this.get(id);
            if (m != null) {
                try {
                    m.disableModule();
                } catch (Throwable ex) {
                    ExceptionUtil.log(ex);
                }
            }
        }

        this.moduleMap.remove(id);
        this.getKnownModuleMetas().remove(id);
    }
}
