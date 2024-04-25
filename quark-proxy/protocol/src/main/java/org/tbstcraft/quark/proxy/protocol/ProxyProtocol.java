package org.tbstcraft.quark.proxy.protocol;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

public interface ProxyProtocol {
    static void writePacketHeaders(ByteBuf buffer, MessageType type, String sender, String channel) {
        buffer.writeInt(type.getId());
        BufferUtil.writeString(buffer, sender);
        BufferUtil.writeString(buffer, channel);
    }
}
