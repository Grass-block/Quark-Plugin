package org.tbstcraft.quark;

import org.tbstcraft.quark.packages.initializer.JsonPackageInitializer;
import org.tbstcraft.quark.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.packages.provider.MultiPackageProvider;
import org.tbstcraft.quark.packages.provider.QuarkPackageProvider;

import java.util.Set;

@QuarkPackageProvider
public class QuarkBase extends MultiPackageProvider {

    @Override
    public Set<PackageInitializer> createInitializers() {
        return Set.of(
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_security.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_display.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_chat.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_utilities.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_automatic.json")
        );
    }
}
