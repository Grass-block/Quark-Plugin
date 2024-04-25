package org.tbstcraft.quark.service;

import me.gb2022.commons.crypto.AESCipher;
import me.gb2022.commons.crypto.CodecCipher;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.util.container.ObjectContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public interface CipherService extends Service {
    ObjectContainer<CipherService> INSTANCE = new ObjectContainer<>();

    static void init() {
        INSTANCE.set(create(Quark.PLUGIN));
        INSTANCE.get().onEnable();
    }

    static CipherService create(Plugin plugin) {
        return new Impl(plugin.getDataFolder());
    }

    static CodecCipher getCipher() {
        return INSTANCE.get().get();
    }

    CodecCipher get();

    final class Impl implements CipherService {
        private final CodecCipher cipher;

        public Impl(File folder) {
            File keyFile = new File(folder.getAbsoluteFile() + "/key");
            this.cipher = new AESCipher(loadKey(keyFile));
        }

        private String loadKey(File keyFile) {
            if (!keyFile.exists() || keyFile.length() == 0) {
                try (FileOutputStream stream = new FileOutputStream(keyFile)) {
                    byte[] data = new byte[4096];
                    new Random().nextBytes(data);
                    stream.write(data);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            try (FileInputStream in = new FileInputStream(keyFile)) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public CodecCipher get() {
            return this.cipher;
        }
    }
}
