package org.tbstcraft.quark;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.foundation.platform.Compatibility;

import java.util.HashMap;
import java.util.Map;

public final class BundledPackageLoader {
    private final Map<String, JavaPlugin> integrated = new HashMap<>();

    public void init() {
        for (String s:new String[]{"quark-base","quark-game","quark-web","quark-proxy"}) {
            if(Bukkit.getPluginManager().getPlugin(s)!=null){
                return;
            }
        }

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
            var type = clazz.get();

            var p = (JavaPlugin) type.getConstructor().newInstance();
            var instance = Quark.getInstance();

            //whatever its legal, screw it!
            p.init(
                    null,
                    Bukkit.getServer(),
                    instance.getDescription(),
                    instance.getDataFolder(),
                    instance.getFile(),
                    instance.getClass().getClassLoader()
                  );

            this.integrated.put(type.getName(), p);
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
