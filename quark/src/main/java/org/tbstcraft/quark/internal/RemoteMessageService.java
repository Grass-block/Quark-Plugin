package org.tbstcraft.quark.internal;

import io.netty.buffer.ByteBuf;
import me.gb2022.apm.remote.APMLoggerManager;
import me.gb2022.apm.remote.RemoteMessenger;
import me.gb2022.apm.remote.connector.RemoteConnector;
import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.service.*;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@QuarkService(id = "remote-message-service")
public interface RemoteMessageService extends Service {
    @ServiceInject
    ServiceHolder<RemoteMessageService> INSTANCE = new ServiceHolder<>();

    @ServiceProvider
    static RemoteMessageService create(ConfigurationSection config) {
        if (!config.getBoolean("enable", true)) {
            return new PHImpl();
        }

        return new Impl(
                config.getString("identifier"),
                new InetSocketAddress(Objects.requireNonNull(config.getString("host")), config.getInt("port")),
                config.getBoolean("proxy"),
                Objects.requireNonNull(config.getString("key")).getBytes(StandardCharsets.UTF_8)
        );
    }

    static RemoteMessageService getInstance() {
        return INSTANCE.get();
    }


    static void addHandler(Object handler) {
        getInstance().addMessageHandler(handler);
    }

    static void removeHandler(Object handler) {
        getInstance().removeMessageHandler(handler);
    }

    static void broadcast(String path, Consumer<ByteBuf> b) {
        getInstance().sendBroadcast(path, b);
    }

    static void message(String receiver, String path, Consumer<ByteBuf> b) {
        getInstance().sendMessage(receiver, path, b);
    }

    static RemoteConnector.ServerQuery query(String receiver, String path, Consumer<ByteBuf> b) {
        return getInstance().sendQuery(receiver, path, b);
    }

    Set<String> getServerInGroup();

    void addMessageHandler(Object handler);

    void removeMessageHandler(Object handler);

    void sendMessage(String target, String channel, ByteBuf msg);

    void sendMessage(String target, String channel, Consumer<ByteBuf> writer);

    void sendBroadcast(String channel, ByteBuf msg);

    void sendBroadcast(String channel, Consumer<ByteBuf> writer);

    RemoteConnector.ServerQuery sendQuery(String target, String channel, ByteBuf msg);

    RemoteConnector.ServerQuery sendQuery(String target, String channel, Consumer<ByteBuf> writer);

    RemoteMessenger getMessenger();

    final class PHImpl implements RemoteMessageService {

        @Override
        public Set<String> getServerInGroup() {
            return Set.of();
        }

        @Override
        public void addMessageHandler(Object handler) {

        }

        @Override
        public void removeMessageHandler(Object handler) {

        }

        @Override
        public void sendMessage(String target, String channel, ByteBuf msg) {

        }

        @Override
        public void sendMessage(String target, String channel, Consumer<ByteBuf> writer) {

        }

        @Override
        public void sendBroadcast(String channel, ByteBuf msg) {

        }

        @Override
        public void sendBroadcast(String channel, Consumer<ByteBuf> writer) {

        }

        @Override
        public RemoteConnector.ServerQuery sendQuery(String target, String channel, ByteBuf msg) {
            return null;
        }

        @Override
        public RemoteConnector.ServerQuery sendQuery(String target, String channel, Consumer<ByteBuf> writer) {
            return null;
        }

        @Override
        public RemoteMessenger getMessenger() {
            return null;
        }
    }

    final class Impl implements RemoteMessageService {
        private final RemoteMessenger messenger;

        public Impl(String id, InetSocketAddress address, boolean proxy, byte[] key) {
            APMLoggerManager.setLoggerCreator((s) -> new Logger(s, null) {
                @Override
                public void log(LogRecord record) {
                    Quark.LOGGER.log(record);
                }
            });
            this.messenger = new RemoteMessenger(proxy, id, address, key);
        }


        @Override
        public void onEnable() {
            RemoteMessageService.super.onEnable();
        }

        @Override
        public void onDisable() {
            this.messenger.stop();
        }

        @Override
        public Set<String> getServerInGroup() {
            return this.messenger.getServerInGroup();
        }

        @Override
        public void addMessageHandler(Object handler) {
            this.messenger.addMessageHandler(handler);
        }

        @Override
        public void removeMessageHandler(Object handler) {
            this.messenger.removeMessageHandler(handler);
        }

        @Override
        public void sendMessage(String target, String channel, ByteBuf msg) {
            this.messenger.sendMessage(target, channel, msg);
        }

        @Override
        public void sendMessage(String target, String channel, Consumer<ByteBuf> writer) {
            this.messenger.sendMessage(target, channel, writer);
        }

        @Override
        public void sendBroadcast(String channel, ByteBuf msg) {
            this.messenger.sendBroadcast(channel, msg);
        }

        @Override
        public void sendBroadcast(String channel, Consumer<ByteBuf> writer) {
            this.messenger.sendBroadcast(channel, writer);
        }

        @Override
        public RemoteConnector.ServerQuery sendQuery(String target, String channel, ByteBuf msg) {
            return this.messenger.sendQuery(target, channel, msg);
        }

        @Override
        public RemoteConnector.ServerQuery sendQuery(String target, String channel, Consumer<ByteBuf> writer) {
            return this.messenger.sendQuery(target, channel, writer);
        }

        @Override
        public RemoteMessenger getMessenger() {
            return this.messenger;
        }
    }
}
