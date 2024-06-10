package org.tbstcraft.quark;

import org.tbstcraft.quark.framework.packages.initializer.JsonPackageInitializer;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.framework.packages.provider.MultiPackageProvider;
import org.tbstcraft.quark.framework.packages.provider.QuarkPackageProvider;

import java.util.Set;

@QuarkPackageProvider
public final class QuarkProxy extends MultiPackageProvider {
    @Override
    public Set<PackageInitializer> createInitializers() {
        return Set.of(
                new JsonPackageInitializer(FeatureAvailability.PREMIUM,"/packages/quark_proxysupport.json"),
                new JsonPackageInitializer(FeatureAvailability.DEMO_AVAILABLE,"/packages/quark_lobby.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM,"/packages/quark_clientsupport.json")
        );
    }
}
