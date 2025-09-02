package org.atcraftmc.quark;

import org.atcraftmc.starlight.framework.FeatureAvailability;
import org.atcraftmc.starlight.framework.packages.initializer.JsonPackageInitializer;
import org.atcraftmc.starlight.framework.packages.initializer.PackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.framework.packages.provider.QuarkPackageProvider;

import java.util.Set;

@QuarkPackageProvider
public final class QuarkLobby extends MultiPackageProvider {
    @Override
    public Set<PackageInitializer> createInitializers() {
        return Set.of(new JsonPackageInitializer(FeatureAvailability.DEMO_AVAILABLE, "/quark-lobby.json"));
    }
}
