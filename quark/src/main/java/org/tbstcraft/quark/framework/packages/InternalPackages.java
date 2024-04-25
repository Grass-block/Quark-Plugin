package org.tbstcraft.quark.framework.packages;

import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.packages.initializer.JsonPackageInitializer;
import org.tbstcraft.quark.service.framework.PackageManager;
import org.tbstcraft.quark.util.container.ObjectContainer;

public interface InternalPackages {
    ObjectContainer<Packages> PACKAGES = new ObjectContainer<>();

    static void registerAll() {
        PACKAGES.set(new Packages());
        PACKAGES.get().registerAll();
    }

    static void unregisterAll() {
        PACKAGES.get().unregisterAll();
    }

    final class Packages {
        private final AbstractPackage[] packages = new AbstractPackage[]{
                new InternalPackage(new JsonPackageInitializer(FeatureAvailability.DEMO_ONLY, "/packages/quark_demo.json"))
        };

        void registerAll() {
            for (AbstractPackage pkg : this.packages) {
                try {
                    PackageManager.registerPackage(pkg);
                } catch (Exception e) {
                    Quark.LOGGER.severe("failed to enable package: " + pkg.getId());
                    Quark.LOGGER.severe(e.getMessage());
                }
            }
            Quark.LOGGER.info("internal packages registered.");
        }

        void unregisterAll() {
            for (AbstractPackage pkg : this.packages) {
                try {
                    PackageManager.unregisterPackage(pkg);
                } catch (Exception e) {
                    Quark.LOGGER.severe("failed to disable package: " + pkg.getId());
                    Quark.LOGGER.severe(e.getMessage());
                }
            }
            Quark.LOGGER.info("internal packages unregistered.");
        }
    }
}
