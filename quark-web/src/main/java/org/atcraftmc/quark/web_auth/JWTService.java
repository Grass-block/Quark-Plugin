package org.atcraftmc.quark.web_auth;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import org.atcraftmc.qlib.config.ConfigEntry;
import org.atcraftmc.quark.web.VertxContextService;
import org.atcraftmc.starlight.framework.service.*;
import org.atcraftmc.starlight.framework.service.ServiceInject;
import org.atcraftmc.starlight.migration.ConfigAccessor;

import java.util.UUID;
import java.util.function.BiConsumer;

@SLService(id = "jwt", impl = JWTService.class)
public final class JWTService implements Service {
    @ServiceInject
    @RegisterAsGlobal
    public static final ServiceHolder<JWTService> INSTANCE = new ServiceHolder<>();

    private final JWTAuth auth;
    private final JWTOptions jwtOptions;

    public JWTService(JWTAuthOptions config, JWTOptions jwtOptions) {
        this.auth = JWTAuth.create(VertxContextService.vertx(), config);
        this.jwtOptions = jwtOptions;
    }

    public static String token(BiConsumer<JWTOptions, JsonObject> options, UUID uid) {
        return INSTANCE.get().createToken(options, uid);
    }

    @ServiceProvider
    public static JWTService create(ConfigEntry config) {
        var cfg = new JWTAuthOptions();

        cfg.setKeyStore(new KeyStoreOptions().setPath(config.value("keystore-path").string())
                                .setPassword(config.value("keystore-secret").string()));
        cfg.addPubSecKey(new PubSecKeyOptions().setAlgorithm("HS256").setBuffer(config.value("key-secret").string()));

        var opt = new JWTOptions();

        opt.setIssuer(config.value("issuer").string());
        opt.setExpiresInSeconds(ConfigAccessor.getInt(config, "expires"));

        return new JWTService(cfg, opt);
    }

    public String createToken(BiConsumer<JWTOptions, JsonObject> options, UUID uid) {
        var opt = new JWTOptions(this.jwtOptions).setAlgorithm("HS256");
        var payload = new JsonObject();

        opt.setSubject(uid.toString());

        if (options != null) {
            options.accept(opt, payload);
        }

        return this.auth.generateToken(payload, opt);
    }

    public void verifyToken(String token) {

    }
}
