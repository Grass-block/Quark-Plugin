package org.tbstcraft.quark;

import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.foundation.platform.Compatibility;

import java.util.HashMap;
import java.util.Map;

public final class BundledPackageLoader {
    private final Map<String, JavaPlugin> integrated = new HashMap<>();

    public void init() {
        construct(() -> Class.forName("org.atcraftmc.quark.QuarkBase"));
        construct(() -> Class.forName("org.atcraftmc.quark.QuarkGame"));
        construct(() -> Class.forName("org.atcraftmc.quark.QuarkWeb"));
        construct(() -> Class.forName("org.atcraftmc.quark.QuarkProxy"));
    }

    public boolean isPresent() {
        return !this.integrated.isEmpty();
    }

    private void construct(Compatibility.ClassAssertion clazz) {
        try {
            this.integrated.put(clazz.get().getName(), (JavaPlugin) clazz.get().getConstructor().newInstance());
        } catch (ClassNotFoundException ignored) {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void register() {
        for (JavaPlugin plugin : this.integrated.values()) {
            plugin.onEnable();
        }
    }

    public void unregister() {
        for (JavaPlugin plugin : this.integrated.values()) {
            plugin.onDisable();
        }
    }
}
