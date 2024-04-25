package org.tbstcraft.quark.service.web;

import com.sun.net.httpserver.HttpServer;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.framework.module.PackageModule;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public final class HTTPServer {
    private final Logger logger = Quark.PLUGIN.getLogger();
    public ExecutorService threadPool;
    private HttpServer server;
    private boolean running = false;

    public void init(String host, int port, int backlog, int threads) {
        this.threadPool = Executors.newFixedThreadPool(threads);
        this.logger.info("http server:" + host + ":" + port);
        if (host == null) {
            return;
        }
        try {
            this.server = HttpServer.create(new InetSocketAddress(host, port), backlog);
            this.server.setExecutor(SharedObjects.SHARED_THREAD_POOL);
            this.server.start();
            this.running = true;
        } catch (IOException e) {
            this.logger.severe("failed to restart http server: " + e.getMessage());
        }
    }

    public void stop() {
        if (!this.running) {
            return;
        }
        this.server.stop(0);
    }

    public void registerHandler(PackageModule handler) {
        if (!this.running) {
            return;
        }
        for (Method m : handler.getClass().getMethods()) {
            HttpRequest annotation = m.getAnnotation(HttpRequest.class);
            if (annotation == null) {
                continue;
            }
            this.server.createContext(annotation.value(), new HttpHandlerAdapter(m, handler));
        }
    }
}
