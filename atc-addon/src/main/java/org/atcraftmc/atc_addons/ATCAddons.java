package org.atcraftmc.atc_addons;

import org.atcraftmc.atc_addons.modules.AC_PotatoWar;
import org.atcraftmc.atc_addons.realistic_survival.AC_RealisticSurvival;
import org.atcraftmc.atc_addons.modules.Guns_DimensionLiberation;
import me.gb2022.modular.FeatureAvailability;
import org.atcraftmc.starlight.framework.packages.SLPackageBuilder;
import org.atcraftmc.starlight.framework.packages.SLPackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.framework.packages.provider.SLPackageProvider;

import java.util.Set;

@SLPackageProvider
public final class ATCAddons extends MultiPackageProvider {

    @Override
    public Set<SLPackageInitializer> createInitializers() {
        return Set.of(SLPackageBuilder.of("atc-addon", FeatureAvailability.BOTH, (i) -> {
            i.module("ac-potato-war", AC_PotatoWar.class);
            i.module("ac-realistic-survival", AC_RealisticSurvival.class);
            i.module("guns-dimension-liberation", Guns_DimensionLiberation.class);

            i.config("atc-addon");
            i.language("atc-addon", "zh_cn");
        }));
    }
}
