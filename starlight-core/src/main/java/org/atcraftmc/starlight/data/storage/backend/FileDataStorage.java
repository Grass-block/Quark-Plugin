package org.atcraftmc.starlight.data.storage.backend;

import me.gb2022.commons.math.SHA;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class FileDataStorage implements DataStorage {
    private final Map<String, byte[]> cache = new HashMap<>();
    private final Map<String, Integer> aliveTimes = new HashMap<>();
    private final Map<String, Integer> saveRequestTimes = new HashMap<>();
    private final File base;

    public FileDataStorage(File base) {
        if (!base.isDirectory()) {
            throw new IllegalArgumentException("Base %s must be a directory".formatted(base.getAbsolutePath()));
        }
        this.base = base;
    }

    static String key(byte[] key) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(key);
        } catch (NoSuchAlgorithmException var7) {
            throw new RuntimeException(var7);
        }

        byte[] digest = messageDigest.digest();
        return SHA.byteArrayToHexString(digest);
    }

    static String cacheKey(byte[] key) {
        return new String(key, StandardCharsets.UTF_8);
    }

    private File handle(byte[] key) {
        return new File(this.base.getAbsolutePath() + File.separator + key(key));
    }

    public void remove(byte[] key) {
        var k = new String(key, StandardCharsets.UTF_8);

        if (!this.cache.containsKey(k)) {
            return;
        }

        var data = this.cache.get(k);

        this.cache.remove(k);
        this.aliveTimes.remove(k);
        this.saveRequestTimes.remove(k);

        try (var stream = new FileOutputStream(handle(key))) {
            stream.write(data);
            stream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public byte[] get(byte[] key) {
        throw new UnsupportedOperationException("NOT DONE YET.");
    }

    @Override
    public boolean contains(byte[] key) {
        return handle(key).exists();
    }

    @Override
    public synchronized void put(byte[] key, byte[] value) {
        throw new UnsupportedOperationException("NOT DONE YET.");
    }

    @Override
    public synchronized void delete(byte[] key) {
        var file = handle(key);
        if (file.exists()) {
            file.delete();
        }

        var ck = cacheKey(key);
        this.cache.remove(ck);
        this.aliveTimes.remove(ck);
        this.saveRequestTimes.remove(ck);
    }

    @Override
    public synchronized long size() {
        return 0;
    }

    @Override
    public void clear() {
        for (var file : Objects.requireNonNull(this.base.listFiles())) {
            file.delete();
        }
        this.aliveTimes.clear();
        this.saveRequestTimes.clear();
        this.cache.clear();
    }
}
