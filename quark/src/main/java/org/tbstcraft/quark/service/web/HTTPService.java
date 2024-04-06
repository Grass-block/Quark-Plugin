package org.tbstcraft.quark.service.web;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.service.Service;
import org.tbstcraft.quark.util.ObjectContainer;

public interface HTTPService extends Service {
    ObjectContainer<HTTPService> INSTANCE = new ObjectContainer<>();

    static void init() {
        INSTANCE.set(create(Quark.CONFIG.getConfig("http-server")));
        INSTANCE.get().onEnable();
    }

    static void stop() {
        INSTANCE.get().onDisable();
    }

    static void registerHandler(PackageModule handler) {
        INSTANCE.get().registerHttpHandler(handler);
    }

    static HTTPService create(ConfigurationSection config) {
        return new ServiceImplementation(config);
    }

    void registerHttpHandler(PackageModule module);

    final class ServiceImplementation implements HTTPService, Service {
        private final HTTPServer server = new HTTPServer();
        private final ConfigurationSection config;

        public ServiceImplementation(ConfigurationSection config) {
            this.config = config;
        }

        @Override
        public void onEnable() {
            String host = this.config.getString("host", "127.0.0.1");
            int port = this.config.getInt("port", 8125);
            int backlog = this.config.getInt("backlog", 10);
            int threads = this.config.getInt("threads", 8);

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
