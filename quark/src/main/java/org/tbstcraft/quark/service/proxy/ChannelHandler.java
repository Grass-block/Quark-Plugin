package org.tbstcraft.quark.service.proxy;

public interface ChannelHandler {
    void onMessageReceived(String channelId,byte[] data,ProxyChannel channel);
}
