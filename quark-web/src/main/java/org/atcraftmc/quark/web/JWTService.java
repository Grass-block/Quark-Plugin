package org.atcraftmc.quark.web;

import io.vertx.ext.auth.jwt.JWTAuth;
import org.atcraftmc.qlib.config.ConfigEntry;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceProvider;

@QuarkService(id = "jwt",impl = JWTService.class)
public class JWTService implements Service {
    private final JWTAuth a

    @ServiceProvider
    public static JWTService create(ConfigEntry config){

    }


    @Override
    public void onEnable() {
        Service.super.onEnable();
    }

    @Override
    public void onDisable() {
        Service.super.onDisable();
    }
}
