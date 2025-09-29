package org.atcraftmc.quark.web_auth;

import io.vertx.core.http.HttpServerRequest;
import org.atcraftmc.quark.web.VertxRouter;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.starlight.framework.module.SLPackageModule;

@ApplicationModule
public class PasswordAuthorization extends SLPackageModule {

    @VertxRouter("/api/auth/password")
    public void passwordAuth(HttpServerRequest request) {
        request.response().putHeader("content-type", "text/plain");
        request.response().setStatusCode(200);

        var username = request.params().get("username");
        var passwordHash = request.params().get("password");

        var passed = PlayerAuthService.verify(username, passwordHash);

    }
}
