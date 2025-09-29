package org.atcraftmc.quark.web;

import org.atcraftmc.quark.web.http.HTTPServer;
import org.atcraftmc.qlib.config.ConfigEntry;
import me.gb2022.modular.service.ApplicationService;
import org.atcraftmc.starlight.framework.SLService;
import me.gb2022.modular.service.ServiceHolder;
import me.gb2022.modular.service.injection.ServiceProvider;
import me.gb2022.modular.service.injection.ServiceInject;
import org.atcraftmc.starlight.migration.ConfigAccessor;

@ApplicationService(id = "http-service")
public interface HttpService extends SLService {
    @ServiceInject
    ServiceHolder<HttpService> INSTANCE = new ServiceHolder<>();

    @ServiceProvider
    static HttpService create(ConfigEntry config) {
        return new ServiceImplementation(config);
    }

    static void registerHandler(Object handler) {
        INSTANCE.get().registerHttpHandler(handler);
    }

    void registerHttpHandler(Object module);

    final class ServiceImplementation implements HttpService, SLService {
        private final HTTPServer server = new HTTPServer();
        private final ConfigEntry config;

        public ServiceImplementation(ConfigEntry config) {
            this.config = config;
        }

        @Override
        public void enable() {
            var host = this.config.value("host").string();
            var port = ConfigAccessor.getInt(config, "port");
            var backlog = ConfigAccessor.getInt(config,"backlog");
            var threads = ConfigAccessor.getInt(config,"threads");

            this.server.init(host, port, backlog, threads);
        }

        @Override
        public void disable() {
            this.server.stop();
        }

        @Override
        public void registerHttpHandler(Object module) {
            this.server.registerHandler(module);
        }
    }
}
