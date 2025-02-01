package org.tbstcraft.quark;

import org.tbstcraft.quark.data.ModuleDataService;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.framework.packages.InternalPackage;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.framework.packages.initializer.PackageBuilderInitializer;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.framework.record.RecordService;
import org.tbstcraft.quark.internal.*;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
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
        return new PackageBuilderInitializer("quark-core", FeatureAvailability.BOTH)
                .service(TaskService.class)
                .service(CacheService.class)
                .service(CommandEventService.class)
                .service(RecordService.class)
                .service(PlaceHolderService.class)
                .service(ModuleDataService.class)
                .service(PlayerDataService.class)
                .service(InternalCommandsProvider.class)
                .service(RemoteMessageService.class)
                .service(UIManager.class)
                .service(PlayerIdentificationService.class)

                .module("demo-warning", DemoWarning.class)
                .module("version-log-viewer", VersionLogViewer.class)
                .module("counter-conflict-handler", CounterPluginConflictHandler.class)
                .module("modrinth-version-check", ModrinthVersionCheck.class)
                .module("custom-language-pack-loader", CustomLanguagePackLoader.class)
                .module("papi-global-vars-injector", PAPIGlobalVarsInjector.class)
                .module("legacy-command-timings-patch", LegacyCommandTimingsPatch.class)
                .module("incomplete-installation-detector", IncompleteInstallationDetector.class)

                .language("quark-core", "zh_cn")
                .language("quark-core", "en_us")
                .language("common", "zh_cn")
                .language("common", "en_us")

                .config("quark-core");
    }
}