package org.atcraftmc.starlight;

import me.gb2022.modular.FeatureAvailability;
import org.atcraftmc.starlight.framework.packages.SLPackageBuilder;
import org.atcraftmc.starlight.framework.packages.SLPackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.framework.packages.provider.SLPackageProvider;
import org.atcraftmc.starlight.core.custom.CustomBlockService;
import org.atcraftmc.starlight.oddities.Elevator;
import org.atcraftmc.starlight.tweaks.*;

import java.util.Set;

@SLPackageProvider
public final class SLGamePackage extends MultiPackageProvider {
    static SLPackageInitializer tweak() {
        return SLPackageBuilder.of("starlight-tweaks", FeatureAvailability.BOTH, (i) -> {
            i.config("starlight-tweaks");
            i.language("starlight-tweaks", "zh_cn");

            i.module(CropClickHarvest.class);
            i.module(DispenserInteraction.class);
            i.module(DoubleDoorSync.class);
            i.module(RealisticSleep.class);
            i.module(VeinMiner.class);
            i.module(PortableFunctionalBlocks.class);
            i.module(PortableShulkerBox.class);
            i.module(ItemDropSecure.class);
            i.module(StairSeat.class);
            i.module(RealisticMinecart.class);
        });
    }

    static SLPackageInitializer oddities() {
        return SLPackageBuilder.of("starlight-oddities", FeatureAvailability.BOTH, (i) -> {
            i.config("starlight-oddities");
            i.language("starlight-oddities", "zh_cn");
            i.module(Elevator.class);
        });
    }

    static Set<SLPackageInitializer> initializers() {
        return Set.of(tweak(), oddities());
    }

    @Override
    public Set<SLPackageInitializer> createInitializers() {
        return initializers();
    }
}
