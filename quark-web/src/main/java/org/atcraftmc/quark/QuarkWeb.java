package org.atcraftmc.quark;

import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.framework.packages.initializer.JsonPackageInitializer;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.framework.packages.provider.MultiPackageProvider;
import org.tbstcraft.quark.framework.packages.provider.QuarkPackageProvider;

import java.util.Set;

@QuarkPackageProvider
public final class QuarkWeb extends MultiPackageProvider {
    @Override
    public Set<PackageInitializer> createInitializers() {
        return initializers();
    }

    public static Set<PackageInitializer> initializers(){
        return Set.of(
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark-web.json")
        );
    }
}
