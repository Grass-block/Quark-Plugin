package web;

import com.google.gson.JsonObject;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.service.PlayerAuthService;
import org.tbstcraft.quark.service.web.HTTPService;
import org.tbstcraft.quark.service.web.HttpHandlerContext;
import org.tbstcraft.quark.service.web.HttpRequest;
import org.tbstcraft.quark.service.web.TokenStorage;

@QuarkModule(version = "0.3-beta")
public class HttpTokenAuthorizer extends PackageModule {
    @Override
    public void enable() {
        HTTPService.registerHandler(this);
    }

    @HttpRequest("/auth/login")
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
        String token = TokenStorage.create();
        obj.addProperty("token", token);
        obj.addProperty("remain", TokenStorage.remain(token));
    }

    @HttpRequest("/auth/logout")
    public void logout(HttpHandlerContext ctx) {
        String token = ctx.getParam("token");
        if (token == null) {
            ctx.error(400, "ERROR:PARAMETER_MISSING");
            return;
        }
        if (!TokenStorage.verify(token)) {
            ctx.error(400, "ERROR:INVALID_TOKEN");
            return;
        }
        ctx.createJsonReturn().addProperty("token", TokenStorage.destroy(token));
    }

    @HttpRequest("/auth/extend")
    public void extend(HttpHandlerContext ctx) {
        String token = ctx.getParam("token");
        String time = ctx.getParam("time");
        if (token == null || time == null) {
            ctx.error(400, "ERROR:PARAMETER_MISSING");
            return;
        }
        if (!TokenStorage.verify(token)) {
            ctx.error(400, "ERROR:INVALID_TOKEN");
            return;
        }

        JsonObject obj = ctx.createJsonReturn();
        obj.addProperty("token", TokenStorage.destroy(token));
        obj.addProperty("remain", TokenStorage.extend(token, Integer.parseInt(time)));
    }
}
