package org.atcraftmc.starlight.framework.module;

import me.gb2022.modular.module.IModule;
import org.atcraftmc.qlib.config.ConfigEntry;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.framework.packages.SLPackage;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.InputStream;

public interface SLModule extends IModule<SLPackage, SLModuleHandle>, Listener {
    Plugin ownerPlugin();

    default LanguageEntry language() {
        return this.handle().getLanguage();
    }

    default ConfigEntry config() {
        return this.handle().getConfig();
    }

    default InputStream getResource(String path) {
        if (!path.startsWith("/")) {
            path = path + "/";
        }
        return this.getClass().getResourceAsStream("/assets" + path);
    }
}
