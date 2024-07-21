package org.tbstcraft.quark.tweaks;

import org.bukkit.event.Listener;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.util.HashMap;
import java.util.Map;

@QuarkModule(version = "1.2.0")
public class VanillaTweaks extends PackageModule {
    private static final Map<String, Listener> FEATURES = new HashMap<>();

    private static void initializeFeatures() {

    }

    @Override
    public void enable() {
        initializeFeatures();
        for (String k : FEATURES.keySet()) {
            if (!this.getConfig().getBoolean(k)) {
                continue;
            }
            BukkitUtil.registerEventListener(FEATURES.get(k));
        }
    }

    @Override
    public void disable() {
        for (String k : FEATURES.keySet()) {
            BukkitUtil.unregisterEventListener(FEATURES.get(k));
        }
    }
}

