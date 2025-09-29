package org.atcraftmc.quark;

import org.atcraftmc.quark.web.SMTPService;
import org.atcraftmc.quark.web.account.AccountActivation;
import me.gb2022.modular.FeatureAvailability;
import org.atcraftmc.starlight.framework.packages.SLPackageBuilder;
import org.atcraftmc.starlight.framework.packages.SLPackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.framework.packages.provider.SLPackageProvider;

import java.util.Set;

@SLPackageProvider
public final class QuarkWeb extends MultiPackageProvider {
    public static Set<SLPackageInitializer> initializers() {
        return Set.of(
                /*
                SLPackageBuilder.of("quark-web", FeatureAvailability.BOTH, (i) -> {
                    i.service(VertxContextService.class);
                    i.service(VertxHttpService.class);
                }),
                SLPackageBuilder.of("quark-web-auth", FeatureAvailability.BOTH, (i) -> {
                    i.service(JWTService.class);
                    i.module("minecraft-sso-authorization", MinecraftSsoAuthorization.class);
                })

                 */

                SLPackageBuilder.of("quark-web", FeatureAvailability.BOTH, (i)->{
                    i.service(SMTPService.class);
                    i.module("account-activation",AccountActivation.class);

                    i.config("quark-web");
                    i.language("quark-web","zh_cn");
                })
        );
    }

    @Override
    public Set<SLPackageInitializer> createInitializers() {
        return initializers();
    }
}
