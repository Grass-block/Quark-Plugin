package org.atcraftmc.quark;

import org.atcraftmc.quark.proxy.*;
import org.atcraftmc.starlight.framework.FeatureAvailability;
import org.atcraftmc.starlight.framework.packages.initializer.PackageBuilderInitializer;
import org.atcraftmc.starlight.framework.packages.initializer.PackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.framework.packages.provider.QuarkPackageProvider;

import java.util.Set;

@QuarkPackageProvider
public final class QuarkProxy extends MultiPackageProvider {
    @Override
    public Set<PackageInitializer> createInitializers() {
        return Set.of(PackageBuilderInitializer.of("quark-proxysupport", FeatureAvailability.BOTH, (i) -> {
            i.module("chat-sync", ChatSync.class);
            i.module("forge-server-teleportation", ForgeServerTeleportation.class);
            i.module("geyser-skin-redirect", GeyserSkinRedirect.class);
            i.module("legacy-forwarding-protect", LegacyForwardingProtect.class);
            i.module("mcsm-dynamic-instance", MCSMDynamicInstance.class);
            i.module("proxy-ping", ProxyPing.class);
            i.module("server-connect", ServerConnect.class);

            i.config("quark-proxysupport");
            i.language("quark-proxysupport", "zh_cn");
        }));
    }
}
