package org.tbstcraft.quark.platform;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Platform libraries for [Quark] plugins has been initialized.");
    }
}
