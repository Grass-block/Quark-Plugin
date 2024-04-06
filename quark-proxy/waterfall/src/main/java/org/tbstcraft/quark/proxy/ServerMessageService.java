package org.tbstcraft.quark.proxy;

import me.gb2022.commons.crypto.AESCipher;
import me.gb2022.commons.crypto.CodecCipher;
import me.gb2022.commons.crypto.DebugCipher;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public interface ServerMessageService {
    Map<String, CodecCipher> MAP = new HashMap<>();

    private static String loadKey(File keyFile) {
        try (FileInputStream in = new FileInputStream(keyFile)) {
            return new String(in.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static CodecCipher getCipher(ServerInfo server) {
        CodecCipher cipher = MAP.get(server.getName());
        if (cipher == null) {
            File folder = new File(QuarkProxy.INSTANCE.getDataFolder().getAbsolutePath() + "/keys");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File key = new File(folder.getAbsolutePath() + "/" + server.getName());
            if (key.exists()) {
                cipher = new AESCipher(loadKey(key));
            } else {
                cipher = new DebugCipher();
            }
            MAP.put(server.getName(), cipher);
        }
        return cipher;
    }

    static void send(ServerInfo server, String channel, byte[] data) {
        server.sendData(channel, Base64.getMimeEncoder().encode(getCipher(server).encode(data)));
    }
}
