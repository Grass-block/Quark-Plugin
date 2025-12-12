package org.atcraftmc.starlight.framework;

import me.gb2022.modular.ObjectOperationResult;
import me.gb2022.modular.module.ModuleManagerV2;
import me.gb2022.modular.service.ApplicationService;
import me.gb2022.modular.service.ServiceHolder;
import me.gb2022.modular.service.ServiceLayer;
import me.gb2022.modular.service.injection.ServiceInject;
import me.gb2022.modular.service.injection.ServiceProvider;
import org.atcraftmc.qlib.PluginConcept;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.api.event.ModuleEvent;
import org.atcraftmc.starlight.foundation.platform.APIProfile;
import org.atcraftmc.starlight.foundation.platform.APIProfileTest;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.SLModuleHandle;
import org.atcraftmc.starlight.migration.DataFix;
import org.atcraftmc.starlight.util.ExceptionUtil;
import org.atcraftmc.starlight.util.FilePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

@ApplicationService(id = "module", layer = ServiceLayer.FRAMEWORK)
public final class SLModuleManager extends ModuleManagerV2<SLModule, SLModuleHandle> implements SLService {
    @ServiceInject
    public static final ServiceHolder<SLModuleManager> INSTANCE = new ServiceHolder<>();

    public static final String DATA_FILE = "%s/data/modules.properties";

    public SLModuleManager(PluginConcept parent) {
        super(parent.logger());
    }

    @ServiceProvider
    public static SLModuleManager create() {
        return new SLModuleManager(Starlight.instance());
    }

    public static SLModuleManager getInstance() {
        return SLModuleManager.INSTANCE.get();
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
                this.logger.error("failed to create status file");
                return file;
            }
            return file;
        }
        return file;
    }

    @Override
    public boolean getDefaultModuleStatus() {
        return Starlight.instance().getConfig().getBoolean("config.default-status.module");
    }

    @Override
    public void saveStatus(Properties meta) {
        try {
            meta.store(new FileOutputStream(this.getStatusFile()), "auto generated file,please don't edit it.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean validRegister(SLModuleHandle handle) {
        for (APIProfile profile : handle.getCompatBlackList()) {
            if (APIProfileTest.getAPIProfile() != profile) {
                continue;
            }
            return false;
        }

        return true;
    }

    @Override
    public void handleException(Throwable ex) {
        ExceptionUtil.log(ex);
    }

    @Override
    public void handlePreEnable(SLModuleHandle meta) {
        BukkitUtil.callEvent(new ModuleEvent.PreEnable(meta));
    }

    @Override
    public void handlePostEnable(SLModuleHandle meta, ObjectOperationResult result) {
        BukkitUtil.callEvent(new ModuleEvent.Enable(meta, result));
    }

    @Override
    public void handlePreDisable(SLModuleHandle meta) {
        BukkitUtil.callEvent(new ModuleEvent.PreDisable(meta));
    }

    @Override
    public void handlePostDisable(SLModuleHandle meta, ObjectOperationResult result) {
        BukkitUtil.callEvent(new ModuleEvent.Disable(meta, result));
    }
}
