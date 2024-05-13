package org.tbstcraft.quark.service.network;

import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.service.*;
import org.tbstcraft.quark.service.network.http.HTTPServer;

@QuarkService(id = "http-service")
public interface HttpService extends Service {
    @ServiceInject
    ServiceHolder<HttpService> INSTANCE = new ServiceHolder<>();

    @ServiceProvider
    static HttpService create(ConfigurationSection config) {
        return new ServiceImplementation(config);
    }

    static void registerHandler(PackageModule handler) {
        INSTANCE.get().registerHttpHandler(handler);
    }

    void registerHttpHandler(PackageModule module);

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
        public void registerHttpHandler(PackageModule module) {
            this.server.registerHandler(module);
        }
    }
}
