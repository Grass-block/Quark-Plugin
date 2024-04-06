package org.tbstcraft.quark;

import org.tbstcraft.quark.packages.initializer.JsonPackageInitializer;
import org.tbstcraft.quark.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.packages.provider.MultiPackageProvider;
import org.tbstcraft.quark.packages.provider.QuarkPackageProvider;

import java.util.Set;

@QuarkPackageProvider
public class QuarkGame extends MultiPackageProvider {
    @Override
    public Set<PackageInitializer> createInitializers() {
        return Set.of(
                new JsonPackageInitializer(FeatureAvailability.PREMIUM,"/packages/quark_tweaks.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM,"/packages/quark_contents.json")
        );
    }
}
