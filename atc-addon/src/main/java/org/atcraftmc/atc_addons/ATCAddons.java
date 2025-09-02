package org.atcraftmc.atc_addons;

import org.atcraftmc.atc_addons.modules.AC_PotatoWar;
import org.atcraftmc.atc_addons.realistic_survival.AC_RealisticSurvival;
import org.atcraftmc.atc_addons.modules.Guns_DimensionLiberation;
import org.atcraftmc.starlight.framework.FeatureAvailability;
import org.atcraftmc.starlight.framework.packages.initializer.PackageBuilderInitializer;
import org.atcraftmc.starlight.framework.packages.initializer.PackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.framework.packages.provider.QuarkPackageProvider;

import java.util.Set;

@QuarkPackageProvider
public final class ATCAddons extends MultiPackageProvider {

    @Override
    public Set<PackageInitializer> createInitializers() {
        return Set.of(PackageBuilderInitializer.of("atc-addon", FeatureAvailability.BOTH, (i) -> {
            i.module("ac-potato-war", AC_PotatoWar.class);
            i.module("ac-realistic-survival", AC_RealisticSurvival.class);
            i.module("guns-dimension-liberation", Guns_DimensionLiberation.class);

            i.config("atc-addon");
            i.language("atc-addon", "zh_cn");
        }));
    }
}
