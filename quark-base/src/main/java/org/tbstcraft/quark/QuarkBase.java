package org.tbstcraft.quark;

import org.tbstcraft.quark.framework.packages.initializer.JsonPackageInitializer;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.framework.packages.provider.MultiPackageProvider;
import org.tbstcraft.quark.framework.packages.provider.QuarkPackageProvider;

import java.util.Set;

@QuarkPackageProvider
public final class QuarkBase extends MultiPackageProvider {

    @Override
    public Set<PackageInitializer> createInitializers() {
        return Set.of(
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_security.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_display.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_chat.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_utilities.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_automatic.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark-management.json")
        );
    }
}
