package org.tbstcraft.quarkservice;

import com.sun.net.httpserver.HttpServer;
import ink.flybird.jflogger.ILogger;
import ink.flybird.jflogger.LogManager;
import org.tbstcraft.quarkservice.handler.ProxyHandler;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;

public class Server {
    ILogger LOGGER = LogManager.getLogger("Server");

    private HttpServer server;

    public void start() {
        TomlParseResult config;
        try {
            config = Toml.parse(Objects.requireNonNull(Server.class.getResourceAsStream("/config.toml")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TomlTable server = config.getTable("server");
        TomlTable proxy = config.getTable("proxy");

        if (server == null) {
            LOGGER.warn("no server config presented.");
            return;
        }

        String host = Objects.requireNonNull(server.getString("ip"));
        int port = Math.toIntExact(Objects.requireNonNull(server.getLong("port")));
        int backlog = Math.toIntExact(Objects.requireNonNull(server.getLong("backlog")));

        try {
            this.server = HttpServer.create(new InetSocketAddress(host, port), backlog);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (proxy != null) {
            for (String path : proxy.keySet()) {
                String targetHost = proxy.getString(path);
                this.server.createContext("/" + path, new ProxyHandler(targetHost, "/" + path));
                LOGGER.info("added proxy %s -> %s.".formatted(path, targetHost));
            }
        } else {
            LOGGER.warn("no proxy presented.");
        }
        this.server.start();
        LOGGER.info("server started on %s:%d.".formatted(host, port));
    }

    public void stop() {
        this.server.stop(0);
        LOGGER.info("server stopped.");
    }
}
