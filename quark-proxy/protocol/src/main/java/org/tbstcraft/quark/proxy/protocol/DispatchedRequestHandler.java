package org.tbstcraft.quark.proxy.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public interface DispatchedRequestHandler {
    default void dispatch(ByteBuf data) {
        int type = data.readInt();
        String sender = BufferUtil.readString(data);
        String channel = BufferUtil.readString(data);

        int length = data.readInt();
        ByteBuf msg = ByteBufAllocator.DEFAULT.buffer();
        data.readBytes(msg, length);

        if (type == 1) {
            String uuid = BufferUtil.readString(data);
            this.onQuery(sender, channel, msg, uuid);
            return;
        }
        this.onMessage(sender, channel, msg);
    }


    void onQuery(String sender, String channel, ByteBuf request, String uuid);

    void onMessage(String sender, String channel, ByteBuf message);
}
