package org.atcraftmc.starlight.util;

import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.ByteBufNIO;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;
import org.bson.io.ByteBufferBsonInput;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public interface BsonCodec {
    BsonDocumentCodec CODEC = new BsonDocumentCodec();

    static byte[] encode(BsonDocument document) {
        var outputBuffer = new BasicOutputBuffer();
        CODEC.encode(new BsonBinaryWriter(outputBuffer), document, EncoderContext.builder().build());
        return outputBuffer.toByteArray();
    }

    static BsonDocument decode(byte[] data) {
        var buf = new ByteBufNIO(ByteBuffer.wrap(data));
        var input = new ByteBufferBsonInput(buf);
        var doc = CODEC.decode(new BsonBinaryReader(input), DecoderContext.builder().build());

        buf.release();

        return doc;
    }

    static String string(BsonDocument document) {
        return new String(encode(document), StandardCharsets.UTF_8);
    }

    static BsonDocument string(String string) {
        return decode(string.getBytes(StandardCharsets.UTF_8));
    }
}
