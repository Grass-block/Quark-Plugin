package org.atcraftmc.starlight;

import me.gb2022.modular.FeatureAvailability;
import org.atcraftmc.starlight.core.RemoteMessageService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;
import org.atcraftmc.starlight.core.ui.UIManager;
import org.atcraftmc.starlight.data.record.RecordService;
import org.atcraftmc.starlight.framework.packages.InternalPackage;
import org.atcraftmc.starlight.framework.packages.SLPackageManager;
import org.atcraftmc.starlight.framework.packages.SLPackageBuilder;
import org.atcraftmc.starlight.framework.packages.SLPackageInitializer;
import org.atcraftmc.starlight.internal.*;

public interface SLInternalPackage {
    static InternalPackage create() {
        return new InternalPackage(initializer());
    }

    static void register(SLPackageManager packageManager) {
        packageManager.addPackage(create());
    }

    static SLPackageInitializer initializer() {
        return new SLPackageBuilder("starlight-core", FeatureAvailability.BOTH)
                .service(TaskService.class)
                .service(CacheService.class)
                .service(RecordService.class)
                .service(PlaceHolderService.class)
                .service(RemoteMessageService.class)
                .service(UIManager.class)
                .service(PlayerIdentificationService.class)
                .service(ChatForwardingService.class)

                .module("version-log-viewer", VersionLogViewer.class)
                .module("modrinth-version-check", ModrinthVersionCheck.class)
                .module("custom-language-pack-loader", CustomLanguagePackLoader.class)

                //platform work-togethers
                .module("papi-support", PAPISupport.class)
                .module("protocol-lib-platform-injector", ProtocolLibPlatformInjector.class)

                //platform patches and internal functions
                .service(InternalServices.BungeeChannelSupplier.class)
                .service(InternalServices.InternalCommandsProvider.class)
                .service(InternalServices.CommandEventService.class)
                .module("platform-patcher", PlatformPatcher.class)
                .module("installation-check", InstallationCheck.class)

                .language("starlight-core", "zh_cn")
                .language("starlight-core", "en_us")
                .language("common", "zh_cn")
                .language("common", "en_us")
                .config("starlight-core");
    }
}