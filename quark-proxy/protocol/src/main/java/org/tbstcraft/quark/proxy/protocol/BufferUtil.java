package org.tbstcraft.quark.proxy.protocol;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public interface BufferUtil {
    static void writeArray(ByteBuf buf, byte[] data) {
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    static byte[] readArray(ByteBuf buf) {
        int len = buf.readInt();
        byte[] data = new byte[len];
        buf.readBytes(data);
        return data;
    }

    static void writeString(ByteBuf buf, String data) {
        writeArray(buf, data.getBytes(StandardCharsets.UTF_8));
    }

    static String readString(ByteBuf buf) {
        return new String(readArray(buf), StandardCharsets.UTF_8);
    }


}
