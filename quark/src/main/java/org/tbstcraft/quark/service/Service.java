package org.tbstcraft.quark.service;

import org.tbstcraft.quark.TaskManager;
import org.tbstcraft.quark.web.TokenStorage;

public interface Service {
    static void init() {
        ModuleDataService.init();
        PlayerAuthService.init();
        PlayerDataService.init();
        WorldEditLocalSessionTracker.init();

        TaskManager.runTimer("quark_core:web:token_update", 0, 20, TokenStorage.UPDATE_TASK);
    }

    static void stop() {
        ModuleDataService.stop();
        PlayerAuthService.stop();
        PlayerDataService.stop();
        WorldEditLocalSessionTracker.stop();

        TaskManager.cancelTask("quark_core:web:token_update");
    }
}
