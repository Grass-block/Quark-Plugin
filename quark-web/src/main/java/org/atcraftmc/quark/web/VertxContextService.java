package org.atcraftmc.quark.web;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.atcraftmc.qlib.config.ConfigEntry;
import org.atcraftmc.starlight.framework.service.*;
import org.atcraftmc.starlight.framework.service.ServiceInject;
import org.atcraftmc.starlight.migration.ConfigAccessor;

//todo: 按注册顺序加载内容
@SLService(id = "vertx-context", impl = VertxContextService.class)
public final class VertxContextService implements Service {
    @ServiceInject
    @RegisterAsGlobal
    public static final ServiceHolder<VertxContextService> HOLDER = new ServiceHolder<>();

    private final Vertx vertx;

    public VertxContextService(VertxOptions opt) {
        this.vertx = Vertx.vertx(opt);
    }

    public static Vertx vertx() {
        return HOLDER.get().get();
    }

    @ServiceProvider
    private VertxContextService create(ConfigEntry config) {
        var cfg = new VertxOptions();

        cfg.setMaxEventLoopExecuteTime(ConfigAccessor.getInt(config, "vertx-max-event-loop-execute-time"));
        cfg.setMaxWorkerExecuteTime(ConfigAccessor.getInt(config,"vertx-max-worker-execute-time"));
        cfg.setWorkerPoolSize(ConfigAccessor.getInt(config,"vertx-worker-pool-size"));
        cfg.setEventLoopPoolSize(ConfigAccessor.getInt(config,"vertx-event-loop-pool-size"));
        cfg.setInternalBlockingPoolSize(ConfigAccessor.getInt(config,"vertx-internal-blocking-pool-size"));
        cfg.setPreferNativeTransport(ConfigAccessor.getBool(config,"vertx-prefer-native-transport"));

        return new VertxContextService(cfg);
    }

    public Vertx get() {
        return this.vertx;
    }
}
