package org.tbstcraft.quark;

import org.tbstcraft.quark.data.ModuleDataService;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.framework.packages.InternalPackage;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.framework.packages.initializer.PackageBuilderInitializer;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.framework.record.RecordService;
import org.tbstcraft.quark.internal.*;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.internal.ui.UIManager;

public interface QuarkInternalPackage {
    static InternalPackage create() {
        return new InternalPackage(initializer());
    }

    static void register(PackageManager packageManager) {
        packageManager.addPackage(create());
    }

    static PackageInitializer initializer() {
        return new PackageBuilderInitializer("quark-core", FeatureAvailability.DEMO_AVAILABLE, false, false)
                .service(TaskService.class)
                .service(CacheService.class)
                .service(CommandEventService.class)
                .service(RecordService.class)
                .service(ModuleDataService.class)
                .service(PlayerDataService.class)
                .service(InternalCommandsProvider.class)
                .service(SMTPService.class)
                .service(HttpService.class)
                .service(RemoteMessageService.class)
                .service(HttpService.class)
                .service(SMTPService.class)
                .service(UIManager.class)

                .module("reserved", Reserved.class)
                .module("demo-warning", DemoWarning.class)
                .module("version-log-viewer", VersionLogViewer.class);
    }
}