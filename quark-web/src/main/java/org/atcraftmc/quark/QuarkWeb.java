package org.atcraftmc.quark;

import org.atcraftmc.quark.web.SMTPService;
import org.atcraftmc.quark.web.account.AccountActivation;
import org.atcraftmc.starlight.framework.FeatureAvailability;
import org.atcraftmc.starlight.framework.packages.initializer.PackageBuilderInitializer;
import org.atcraftmc.starlight.framework.packages.initializer.PackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.framework.packages.provider.QuarkPackageProvider;

import java.util.Set;

@QuarkPackageProvider
public final class QuarkWeb extends MultiPackageProvider {
    public static Set<PackageInitializer> initializers() {
        return Set.of(
                /*
                PackageBuilderInitializer.of("quark-web", FeatureAvailability.BOTH, (i) -> {
                    i.service(VertxContextService.class);
                    i.service(VertxHttpService.class);
                }),
                PackageBuilderInitializer.of("quark-web-auth", FeatureAvailability.BOTH, (i) -> {
                    i.service(JWTService.class);
                    i.module("minecraft-sso-authorization", MinecraftSsoAuthorization.class);
                })

                 */

                PackageBuilderInitializer.of("quark-web",FeatureAvailability.BOTH,(i)->{
                    i.service(SMTPService.class);
                    i.module("account-activation",AccountActivation.class);

                    i.config("quark-web");
                    i.language("quark-web","zh_cn");
                })
        );
    }

    @Override
    public Set<PackageInitializer> createInitializers() {
        return initializers();
    }
}
