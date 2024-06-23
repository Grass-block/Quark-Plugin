package org.atcraftmc.quark;

import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.framework.packages.provider.MultiPackageProvider;

import java.util.Set;

public class QuarkMiniGame extends MultiPackageProvider {
    @Override
    public Set<PackageInitializer> createInitializers() {
        return Set.of();
    }
}
