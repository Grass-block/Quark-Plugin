package org.tbstcraft.quark.web;

import com.sun.net.httpserver.HttpServer;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.SharedContext;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public final class HTTPServer {
    private final Logger logger = Quark.PLUGIN.getLogger();
    ConfigurationSection config = Quark.CONFIG.getConfig();
    private HttpServer server;

    public void init() {
        if (this.config == null) {
            throw new RuntimeException("no config present!");
        }
        String host = this.config.getString("http_server_host", "127.0.0.1");
        int port = this.config.getInt("http_server_port");
        this.logger.info("http server:" + host + ":" + port);
        try {
            this.server = HttpServer.create(new InetSocketAddress(host, port), 10);
            this.server.setExecutor(SharedContext.SHARED_THREAD_POOL);
            this.server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        this.server.stop(0);
    }

    public void registerHandler(Object handler) {
        for (Method m : handler.getClass().getMethods()) {
            HttpRequest annotation = m.getAnnotation(HttpRequest.class);
            if (annotation == null) {
                continue;
            }
            this.server.createContext(annotation.value(), new HttpHandlerAdapter(m, handler));
        }
    }
}
