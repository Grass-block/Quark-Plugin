package org.atcraftmc.starlight.framework.packages.provider;

import org.atcraftmc.starlight.ProductInfo;
import org.atcraftmc.starlight.Starlight;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.atcraftmc.starlight.framework.packages.AbstractPackage;

import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface PackageProvider {
    Set<AbstractPackage> createPackages();

    default boolean isCoreContextMatch() {
        Plugin quarkInstance = Bukkit.getPluginManager().getPlugin(ProductInfo.CORE_ID);
        if (quarkInstance == null) {
            return false;
        }
        String newId;
        try {
            newId = (String) quarkInstance.getClass().getMethod("getInstanceId").invoke(quarkInstance);
        } catch (Exception e) {
            return false;
        }
        return Objects.equals(this.getCoreInstanceId(), newId);
    }

    default boolean isCoreExist() {
        Logger logger = this.getLogger();

        try {
            Class.forName("org.atcraftmc.starlight.Starlight");
        } catch (ClassNotFoundException e) {
            logger.severe("cannot detect running quark core. consider reload your server.");
            return false;
        }

        return Starlight.instance().isPluginInitialized();
    }

    Logger getLogger();

    String getCoreInstanceId();
}
