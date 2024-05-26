package org.tbstcraft.quark.internal;

import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.framework.packages.InternalPackage;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.framework.packages.initializer.PackageBuilderInitializer;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.service.network.HttpService;
import org.tbstcraft.quark.service.network.RemoteMessageService;
import org.tbstcraft.quark.internal.*;
import org.tbstcraft.quark.internal.data.ModuleDataService;
import org.tbstcraft.quark.internal.data.PlayerDataService;
import org.tbstcraft.quark.internal.record.RecordService;
import org.tbstcraft.quark.service.network.SMTPService;

public interface QuarkInternalPackage {
    static InternalPackage create() {
        return new InternalPackage(initializer());
    }

    static void register(PackageManager packageManager) {
        packageManager.addPackage(create());
    }

    static PackageInitializer initializer() {
        return new PackageBuilderInitializer("quark-core", FeatureAvailability.DEMO_AVAILABLE, false, false)
                .service(CacheService.class)
                .service(CommandEventService.class)
                .service(RecordService.class)
                .service(ModuleDataService.class)
                .service(PlayerDataService.class)

                .service(RemoteMessageService.class)
                .service(HttpService.class)
                .service(SMTPService.class)

                .module("advertisements", Advertisements.class)
                .module("reserved", Reserved.class)
                .module("demo-warning", DemoWarning.class);
    }
}