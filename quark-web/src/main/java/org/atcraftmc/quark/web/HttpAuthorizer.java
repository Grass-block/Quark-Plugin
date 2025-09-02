package org.atcraftmc.quark.web;

import com.google.gson.JsonObject;
import org.atcraftmc.quark.web.http.HttpHandlerContext;
import org.atcraftmc.quark.web_auth.PlayerAuthService;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;

@SLModule(version = "0.3-beta")
public final class HttpAuthorizer extends PackageModule {
    @Override
    public void enable() {
        HttpService.registerHandler(this);
    }

    public void login(HttpHandlerContext ctx) {
        String name = ctx.getParam("name");
        String password = ctx.getParam("password");

        if (name == null || password == null) {
            ctx.error(400, "ERROR:PARAMETER_MISSING");
            return;
        }
        if (!PlayerAuthService.verify(name, password)) {
            ctx.error(400, "ERROR:AUTHORIZE_FAILED");
            return;
        }
        JsonObject obj = ctx.createJsonReturn();
        String token = TokenStorageService.create();
        obj.addProperty("token", token);
        obj.addProperty("remain", TokenStorageService.remain(token));
    }

    public void logout(HttpHandlerContext ctx) {
        String token = ctx.getParam("token");
        if (token == null) {
            ctx.error(400, "ERROR:PARAMETER_MISSING");
            return;
        }
        if (!TokenStorageService.verify(token)) {
            ctx.error(400, "ERROR:INVALID_TOKEN");
            return;
        }
        ctx.createJsonReturn().addProperty("token", TokenStorageService.destroy(token));
    }

    public void extend(HttpHandlerContext ctx) {
        String token = ctx.getParam("token");
        String time = ctx.getParam("time");
        if (token == null || time == null) {
            ctx.error(400, "ERROR:PARAMETER_MISSING");
            return;
        }
        if (!TokenStorageService.verify(token)) {
            ctx.error(400, "ERROR:INVALID_TOKEN");
            return;
        }

        JsonObject obj = ctx.createJsonReturn();
        obj.addProperty("token", TokenStorageService.destroy(token));
        obj.addProperty("remain", TokenStorageService.extend(token, Integer.parseInt(time)));
    }

    public void authMinecraft(HttpHandlerContext ctx) {

    }
}
