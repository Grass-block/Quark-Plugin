package org.atcraftmc.quark;

import org.atcraftmc.quark.proxy.*;
import me.gb2022.modular.FeatureAvailability;
import org.atcraftmc.starlight.framework.packages.SLPackageBuilder;
import org.atcraftmc.starlight.framework.packages.SLPackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.framework.packages.provider.SLPackageProvider;

import java.util.Set;

@SLPackageProvider
public final class QuarkProxy extends MultiPackageProvider {
    @Override
    public Set<SLPackageInitializer> createInitializers() {
        return Set.of(SLPackageBuilder.of("quark-proxysupport", FeatureAvailability.BOTH, (i) -> {
            i.module("chat-sync", ChatSync.class);
            i.module("forge-server-teleportation", ForgeServerTeleportation.class);
            i.module("geyser-skin-redirect", GeyserSkinRedirect.class);
            i.module("legacy-forwarding-protect", LegacyForwardingProtect.class);
            i.module("mcsm-dynamic-instance", MCSMDynamicInstance.class);
            i.module("proxy-ping", ProxyPing.class);
            i.module("server-connect", ClientTransferSupport.class);

            i.config("quark-proxysupport");
            i.language("quark-proxysupport", "zh_cn");
        }));
    }
}
