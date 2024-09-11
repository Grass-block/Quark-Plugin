package org.atcraftmc.quark;

import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.framework.packages.initializer.JsonPackageInitializer;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.framework.packages.provider.MultiPackageProvider;
import org.tbstcraft.quark.framework.packages.provider.QuarkPackageProvider;

import java.util.Set;

@QuarkPackageProvider
public final class QuarkLobby extends MultiPackageProvider {
    @Override
    public Set<PackageInitializer> createInitializers() {
        return Set.of(new JsonPackageInitializer(FeatureAvailability.DEMO_AVAILABLE, "/quark-lobby.json"));
    }
}
