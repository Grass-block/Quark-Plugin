package org.tbstcraft.quark.internal;

import io.netty.buffer.ByteBuf;
import me.gb2022.apm.remote.RemoteMessenger;
import me.gb2022.apm.remote.RemoteQuery;
import me.gb2022.apm.remote.connector.RemoteConnector;
import me.gb2022.apm.remote.event.MessengerEventChannel;
import me.gb2022.apm.remote.event.channel.MessageChannel;
import org.atcraftmc.qlib.config.ConfigEntry;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.service.*;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@QuarkService(id = "remote-message-service")
public class RemoteMessageService implements Service {
    public static final Runnable EMPTY_ACTION = () -> {};

    @RegisterAsGlobal
    @ServiceInject
    public static final ServiceHolder<RemoteMessageService> INSTANCE = new ServiceHolder<>();

    @ServiceProvider
    public static RemoteMessageService create(ConfigEntry config) {
        if (!config.getBoolean("enable")) {
            return new RemoteMessageService();
        }

        return new SimpleImplementation(
                config.getString("identifier"),
                new InetSocketAddress(Objects.requireNonNull(config.getString("host")), config.getInt("port")),
                config.getBoolean("proxy"),
                Objects.requireNonNull(config.getString("key")).getBytes(StandardCharsets.UTF_8)
        );
    }

    public static RemoteMessageService instance() {
        return INSTANCE.get();
    }

    public static RemoteMessenger messenger() {
        if (instance() instanceof SimpleImplementation i) {
            return i.getMessenger();
        }

        throw new IllegalStateException("non implemented instance");
    }

    static RemoteConnector connector() {
        if (instance() instanceof SimpleImplementation i) {
            return i.getConnector();
        }

        throw new IllegalStateException("non implemented instance");
    }

    public void registerEventHandler(Object handler) {
    }

    public void registerEventHandler(Class<?> handler) {
    }

    public void removeMessageHandler(Object handler) {
    }

    public void removeMessageHandler(Class<?> handler) {
    }

    public String getIdentifier() {
        return "";
    }

    public MessengerEventChannel eventChannel() {
        return null;
    }

    public MessageChannel messageChannel(String channel) {
        return null;
    }

    public String message(String uuid, String target, String channel, ByteBuf msg) {
        return UUID.randomUUID().toString();
    }

    public String message(String target, String channel, ByteBuf msg) {
        return UUID.randomUUID().toString();
    }

    public String broadcast(String channel, ByteBuf msg) {
        return UUID.randomUUID().toString();
    }

    public RemoteQuery<ByteBuf> query(String target, String channel, ByteBuf msg) {
        return new RemoteQuery<>(UUID.randomUUID().toString(), ByteBuf.class, (u) -> {});
    }

    public String message(String uuid, String target, String channel, Consumer<ByteBuf> writer) {
        return UUID.randomUUID().toString();
    }

    public String message(String target, String channel, Consumer<ByteBuf> writer) {
        return UUID.randomUUID().toString();
    }

    public String broadcast(String channel, Consumer<ByteBuf> writer) {
        return UUID.randomUUID().toString();
    }

    public RemoteQuery<ByteBuf> query(String target, String channel, Consumer<ByteBuf> writer) {
        return new RemoteQuery<>(UUID.randomUUID().toString(), ByteBuf.class, (u) -> {});
    }

    public <I> String message(String uuid, String target, String channel, I object) {
        return UUID.randomUUID().toString();
    }

    public <I> String message(String target, String channel, I object) {
        return UUID.randomUUID().toString();
    }

    public <I> String broadcast(String channel, I object) {
        return UUID.randomUUID().toString();
    }

    public <I> RemoteQuery<I> query(String target, String channel, I msg) {
        return (RemoteQuery<I>) new RemoteQuery<>(UUID.randomUUID().toString(), msg.getClass(), (u) -> {});
    }

    static final class SimpleImplementation extends RemoteMessageService {
        private final RemoteMessenger messenger;

        public SimpleImplementation(String id, InetSocketAddress address, boolean proxy, byte[] key) {
            this.messenger = new RemoteMessenger(proxy, id, address, key);
            this.messenger.connector().debug(true);
        }


        @Override
        public void registerEventHandler(Object handler) {
            this.messenger.registerEventHandler(handler);
        }

        @Override
        public void registerEventHandler(Class<?> handler) {
            this.messenger.registerEventHandler(handler);
        }

        @Override
        public void removeMessageHandler(Object handler) {
            this.messenger.removeMessageHandler(handler);
        }

        @Override
        public void removeMessageHandler(Class<?> handler) {
            this.messenger.removeMessageHandler(handler);
        }

        public RemoteConnector getConnector() {
            return this.messenger.connector();
        }

        public RemoteMessenger getMessenger() {
            return messenger;
        }

        @Override
        public String getIdentifier() {
            return this.messenger.getIdentifier();
        }

        @Override
        public MessengerEventChannel eventChannel() {
            return this.messenger.eventChannel();
        }

        @Override
        public MessageChannel messageChannel(String channel) {
            return this.messenger.messageChannel(channel);
        }

        @Override
        public String message(String uuid, String target, String channel, ByteBuf msg) {
            return this.messenger.message(uuid, target, channel, msg);
        }

        @Override
        public String message(String target, String channel, ByteBuf msg) {
            return this.messenger.message(target, channel, msg);
        }

        @Override
        public String broadcast(String channel, ByteBuf msg) {
            return this.messenger.broadcast(channel, msg);
        }

        @Override
        public RemoteQuery<ByteBuf> query(String target, String channel, ByteBuf msg) {
            return this.messenger.query(target, channel, msg);
        }

        @Override
        public String message(String uuid, String target, String channel, Consumer<ByteBuf> writer) {
            return this.messenger.message(uuid, target, channel, writer);
        }

        @Override
        public String message(String target, String channel, Consumer<ByteBuf> writer) {
            return this.messenger.message(target, channel, writer);
        }

        @Override
        public String broadcast(String channel, Consumer<ByteBuf> writer) {
            return this.messenger.broadcast(channel, writer);
        }

        @Override
        public RemoteQuery<ByteBuf> query(String target, String channel, Consumer<ByteBuf> writer) {
            return this.messenger.query(target, channel, writer);
        }

        @Override
        public <I> String message(String uuid, String target, String channel, I object) {
            return this.messenger.message(uuid, target, channel, object);
        }

        @Override
        public <I> String message(String target, String channel, I object) {
            return this.messenger.message(target, channel, object);
        }

        @Override
        public <I> String broadcast(String channel, I object) {
            return this.messenger.broadcast(channel, object);
        }

        @Override
        public <I> RemoteQuery<I> query(String target, String channel, I msg) {
            return this.messenger.query(target, channel, msg);
        }
    }
}
