package org.tbstcraft.quark.service.proxy;

import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.service.CipherService;
import org.tbstcraft.quark.service.Service;
import org.tbstcraft.quark.util.ObjectContainer;

import java.util.HashMap;
import java.util.Map;

public interface ProxyMessageService extends Service {
    ObjectContainer<ProxyMessageService> INSTANCE = new ObjectContainer<>();

    static void init() {
        INSTANCE.set(new ServiceImplementation(Quark.PLUGIN));
    }

    static ProxyChannel getChannel(String id) {
        return INSTANCE.get().get(id);
    }

    static void addMessageHandler(String id, ChannelHandler handler) {
        INSTANCE.get().addHandler(id, handler);
    }

    ProxyChannel get(String id);

    void addHandler(String id, ChannelHandler handler);

    final class ServiceImplementation implements ProxyMessageService {
        private final Plugin plugin;
        private final Map<String, ProxyChannel> channels = new HashMap<>();

        public ServiceImplementation(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public ProxyChannel get(String id) {
            ProxyChannel channel = this.channels.get(id);
            if (channel == null) {
                channel = new ProxyChannel(CipherService.getCipher(), this.plugin, id);
                channel.register();
                this.channels.put(id, channel);
            }
            return channel;
        }

        @Override
        public void addHandler(String id, ChannelHandler handler) {
            getChannel(id).addHandler(handler);
        }
    }
}
