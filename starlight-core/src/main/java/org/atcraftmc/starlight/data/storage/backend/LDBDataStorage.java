package org.atcraftmc.starlight.data.storage.backend;

import me.gb2022.commons.container.Pair;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public final class LDBDataStorage implements DataStorage {
    private final DB db;

    public LDBDataStorage(File folder) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("folder is not a directory: " + folder);
        }

        try {
            this.db = new JniDBFactory().open(folder, new Options());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void put(byte[] key, byte[] data) {
        this.db.put(key, data);
    }

    @Override
    public byte[] get(byte[] key) {
        return this.db.get(key);
    }

    @Override
    public void delete(byte[] key) {
        this.db.delete(key);
    }

    @Override
    public boolean contains(byte[] key) {
        return this.db.get(key) != null;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public void clear() {
        var b = this.db.createWriteBatch();
        this.db.forEach((e) -> b.delete(e.getKey()));
        this.db.write(b);
    }

    @Override
    public void close() throws IOException {
        this.db.close();
    }

    @Override
    public void putBatch(Set<Pair<byte[], byte[]>> batch) {
        var b = this.db.createWriteBatch();

        for (var pair : batch) {
            b.put(pair.getLeft(), pair.getRight());
        }

        this.db.write(b);
    }
}
