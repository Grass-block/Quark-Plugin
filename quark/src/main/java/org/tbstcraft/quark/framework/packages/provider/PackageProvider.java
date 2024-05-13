package org.tbstcraft.quark.framework.packages.provider;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.packages.AbstractPackage;

import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface PackageProvider {
    Set<AbstractPackage> createPackages();

    default boolean isCoreContextMatch() {
        Plugin quarkInstance = Bukkit.getPluginManager().getPlugin(Quark.PLUGIN_ID);
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

    default boolean isCoreExist(){
        Logger logger = this.getLogger();

        try {
            Class.forName("org.tbstcraft.quark.Quark");
        } catch (ClassNotFoundException e) {
            logger.severe("cannot detect running quark core. consider reload your server.");
            return false;
        }

        return true;
    }

    Logger getLogger();

    String getCoreInstanceId();
}
