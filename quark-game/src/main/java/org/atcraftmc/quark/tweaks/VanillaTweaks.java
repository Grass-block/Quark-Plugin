package org.atcraftmc.quark.tweaks;

import org.bukkit.event.Listener;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;

import java.util.HashMap;
import java.util.Map;

@SLModule(version = "1.2.0")
public class VanillaTweaks extends PackageModule {
    private static final Map<String, Listener> FEATURES = new HashMap<>();

    private static void initializeFeatures() {

    }

    @Override
    public void enable() {
        initializeFeatures();
        for (String k : FEATURES.keySet()) {
            if (!ConfigAccessor.getBool(this.getConfig(), k)) {
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

