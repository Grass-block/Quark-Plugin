package org.atcraftmc.quark;

import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.framework.packages.initializer.JsonPackageInitializer;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.framework.packages.provider.MultiPackageProvider;
import org.tbstcraft.quark.framework.packages.provider.QuarkPackageProvider;

import java.util.Set;

@QuarkPackageProvider
public final class QuarkGame extends MultiPackageProvider {
    @Override
    public Set<PackageInitializer> createInitializers() {
        return Set.of(
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark-tweaks.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark-storage.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark-contents.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark-warps.json")
                     );
    }
}