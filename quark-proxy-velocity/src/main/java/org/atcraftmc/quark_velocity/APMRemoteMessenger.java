package org.atcraftmc.quark_velocity;

import me.gb2022.apm.remote.RemoteMessenger;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public final class APMRemoteMessenger {
    private RemoteMessenger connector;

    public RemoteMessenger getConnector() {
        return this.connector;
    }

    public void init() {
        if (!Config.featureEnabled("apm-service")) {
            return;
        }

        var config = Config.entry("apm-service");

        var id = config.getString("identifier");
        var host = config.getString("host");
        var port = config.getInt("port");
        var key = config.getString("key").getBytes(StandardCharsets.UTF_8);

        var binding = new InetSocketAddress(host, port);

        if (!config.getBoolean("exchange")) {
            this.connector = new RemoteMessenger(false, id, binding, key);
        } else {
            this.connector = new RemoteMessenger(true, id, binding, key);
        }
    }

    public void stop() {
        if (!Config.featureEnabled("apm-service")) {
            return;
        }
        this.connector.stop();
    }
}
