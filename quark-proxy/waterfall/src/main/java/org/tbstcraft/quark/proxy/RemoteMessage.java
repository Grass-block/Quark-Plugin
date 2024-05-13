package org.tbstcraft.quark.proxy;

import com.moandjiezana.toml.Toml;
import me.gb2022.apm.remote.APMLoggerManager;
import me.gb2022.apm.remote.RemoteMessenger;
import me.gb2022.commons.container.ObjectContainer;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public interface RemoteMessage {
    ObjectContainer<RemoteMessenger> CONNECTOR = new ObjectContainer<>();

    static RemoteMessenger getMessenger() {
        return CONNECTOR.get();
    }

    static void init(Toml table) {
        APMLoggerManager.setLoggerCreator((s) -> new Logger(s, null) {
            @Override
            public void log(LogRecord record) {
                QuarkProxy.LOGGER.log(record);
            }
        });

        String id = table.getString("identifier");
        InetSocketAddress binding = new InetSocketAddress(table.getString("address"), ((int) ((long) table.getLong("port"))));
        byte[] key = table.getString("key").getBytes(StandardCharsets.UTF_8);

        if (Objects.equals(table.getString("role"), "server")) {
            CONNECTOR.set(new RemoteMessenger(false, id, binding, key));
        } else {
            CONNECTOR.set(new RemoteMessenger(true, id, binding, key));
        }
    }

    static void stop() {
        CONNECTOR.get().stop();
    }
}
