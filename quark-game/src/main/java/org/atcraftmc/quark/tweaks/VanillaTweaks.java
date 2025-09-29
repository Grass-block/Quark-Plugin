package org.atcraftmc.quark.tweaks;

import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.event.Listener;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import me.gb2022.modular.module.ApplicationModule;

import java.util.HashMap;
import java.util.Map;

@ApplicationModule(version = "1.2.0")
public class VanillaTweaks extends SLPackageModule {
    private static final Map<String, Listener> FEATURES = new HashMap<>();

    private static void initializeFeatures() {

    }

    @Override
    public void enable() {
        initializeFeatures();
        for (String k : FEATURES.keySet()) {
            if (!ConfigAccessor.getBool(this.config(), k)) {
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

