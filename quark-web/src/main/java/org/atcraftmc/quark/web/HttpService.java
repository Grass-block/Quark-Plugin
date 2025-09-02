package org.atcraftmc.quark.web;

import org.atcraftmc.quark.web.http.HTTPServer;
import org.atcraftmc.qlib.config.ConfigEntry;
import org.atcraftmc.starlight.framework.service.SLService;
import org.atcraftmc.starlight.framework.service.Service;
import org.atcraftmc.starlight.framework.service.ServiceHolder;
import org.atcraftmc.starlight.framework.service.ServiceProvider;
import org.atcraftmc.starlight.framework.service.ServiceInject;
import org.atcraftmc.starlight.migration.ConfigAccessor;

@SLService(id = "http-service")
public interface HttpService extends Service {
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

    final class ServiceImplementation implements HttpService, Service {
        private final HTTPServer server = new HTTPServer();
        private final ConfigEntry config;

        public ServiceImplementation(ConfigEntry config) {
            this.config = config;
        }

        @Override
        public void onEnable() {
            var host = this.config.value("host").string();
            var port = ConfigAccessor.getInt(config, "port");
            var backlog = ConfigAccessor.getInt(config,"backlog");
            var threads = ConfigAccessor.getInt(config,"threads");

            this.server.init(host, port, backlog, threads);
        }

        @Override
        public void onDisable() {
            this.server.stop();
        }

        @Override
        public void registerHttpHandler(Object module) {
            this.server.registerHandler(module);
        }
    }
}
