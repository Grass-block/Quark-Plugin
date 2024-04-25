package org.tbstcraft.quark.proxy.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

public abstract class Connector {
    private final String id;
    private final ChannelHandlerContext context;

    protected Connector(String id, ChannelHandlerContext context) {
        this.id = id;
        this.context = context;
    }

    public String getId() {
        return id;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }

    public void sendMessage(MessageType type, String channel, ByteBuf message) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.ioBuffer();

        this.getContext().writeAndFlush(buffer);
    }


}
