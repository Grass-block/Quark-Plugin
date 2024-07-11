package org.atcraftmc.quark.web;

import org.atcraftmc.quark.web.http.HTTPServer;
import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.framework.service.*;

@QuarkService(id = "http-service")
public interface HttpService extends Service {
    @ServiceInject
    ServiceHolder<HttpService> INSTANCE = new ServiceHolder<>();

    @ServiceProvider
    static HttpService create(ConfigurationSection config) {
        return new ServiceImplementation(config);
    }

    static void registerHandler(Object handler) {
        INSTANCE.get().registerHttpHandler(handler);
    }

    void registerHttpHandler(Object module);

    final class ServiceImplementation implements HttpService, Service {
        private final HTTPServer server = new HTTPServer();
        private final ConfigurationSection config;

        public ServiceImplementation(ConfigurationSection config) {
            this.config = config;
        }

        @Override
        public void onEnable() {
            String host = this.config.getString("host");
            int port = this.config.getInt("port");
            int backlog = this.config.getInt("backlog");
            int threads = this.config.getInt("threads");

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
