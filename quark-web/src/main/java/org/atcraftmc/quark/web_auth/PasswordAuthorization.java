package org.atcraftmc.quark.web_auth;

import io.vertx.core.http.HttpServerRequest;
import org.atcraftmc.quark.web.VertxRouter;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;

@SLModule
public class PasswordAuthorization extends PackageModule {

    @VertxRouter("/api/auth/password")
    public void passwordAuth(HttpServerRequest request) {
        request.response().putHeader("content-type", "text/plain");
        request.response().setStatusCode(200);

        var username = request.params().get("username");
        var passwordHash = request.params().get("password");

        var passed = PlayerAuthService.verify(username, passwordHash);

    }
}
