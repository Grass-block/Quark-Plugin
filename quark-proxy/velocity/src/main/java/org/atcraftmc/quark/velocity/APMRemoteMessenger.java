package org.atcraftmc.quark.velocity;

import com.moandjiezana.toml.Toml;
import me.gb2022.apm.remote.APMLoggerManager;
import me.gb2022.apm.remote.RemoteMessenger;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class APMRemoteMessenger {
    private final QuarkVelocity plugin;
    private Toml config;
    private RemoteMessenger connector;

    public APMRemoteMessenger(QuarkVelocity plugin) {
        this.plugin = plugin;
        APMLoggerManager.setLoggerCreator((s) -> new Logger(s, null) {
            @Override
            public void log(LogRecord record) {
                if (record.getLevel() == Level.INFO) {
                    plugin.getLogger().info(record.getMessage());
                }
                if (record.getLevel() == Level.SEVERE) {
                    plugin.getLogger().error(record.getMessage());
                }
                if (record.getLevel() == Level.WARNING) {
                    plugin.getLogger().warn(record.getMessage());
                }
            }
        });
    }

    public RemoteMessenger getConnector() {
        return this.connector;
    }

    public void init() {
        this.config = plugin.getConfig().entry("apm-messenger");

        if (!this.config.getBoolean("enable")) {
            return;
        }

        String id = this.config.getString("identifier");
        InetSocketAddress binding = new InetSocketAddress(this.config.getString("host"), ((int) ((long) this.config.getLong("port"))));
        byte[] key = this.config.getString("key").getBytes(StandardCharsets.UTF_8);

        if (Objects.equals(this.config.getString("role"), "server")) {
            this.connector = new RemoteMessenger(false, id, binding, key);
        } else {
            this.connector = new RemoteMessenger(true, id, binding, key);
        }
    }

    public QuarkVelocity getPlugin() {
        return plugin;
    }

    public void stop() {
        if (!this.config.getBoolean("enable")) {
            return;
        }
        this.connector.stop();
    }
}
