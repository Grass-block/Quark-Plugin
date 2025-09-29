package org.atcraftmc.quark;

import me.gb2022.modular.FeatureAvailability;
import org.atcraftmc.quark.lobby.BackToSpawn;
import org.atcraftmc.quark.lobby.DefaultInventory;
import org.atcraftmc.quark.lobby.MapProtect;
import org.atcraftmc.quark.lobby.PlayerProtect;
import org.atcraftmc.starlight.framework.packages.SLPackageBuilder;
import org.atcraftmc.starlight.framework.packages.SLPackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.framework.packages.provider.SLPackageProvider;

import java.util.Set;

@SLPackageProvider
public final class SLLobbyPackage extends MultiPackageProvider {
    @Override
    public Set<SLPackageInitializer> createInitializers() {
        return Set.of(SLPackageBuilder.of("starlight-lobby", FeatureAvailability.BOTH, (i) -> {
            i.module(BackToSpawn.class);
            i.module(DefaultInventory.class);
            i.module(MapProtect.class);
            i.module(PlayerProtect.class);

            i.config("starlight-lobby");
            i.language("starlight-lobby", "zh_cn");
            i.language("starlight-lobby", "en_us");
        }));
    }
}
