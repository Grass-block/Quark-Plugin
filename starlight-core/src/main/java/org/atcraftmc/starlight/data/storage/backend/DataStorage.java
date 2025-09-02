package org.atcraftmc.starlight.data.storage.backend;

import me.gb2022.commons.container.Pair;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public interface DataStorage extends Closeable {

    static DataStorage detect(File folder) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("folder %s is not a directory".formatted(folder.getAbsolutePath()));
        }

        if (Objects.requireNonNull(folder.listFiles()).length == 0) {
            return new LDBDataStorage(folder);
        }

        if (new File(folder.getAbsolutePath() + "/LOG").exists()) {
            return new LDBDataStorage(folder);
        }

        return new FileDataStorage(folder); //todo:not done
    }

    void put(byte[] key, byte[] data);

    byte[] get(byte[] key);

    void delete(byte[] key);

    boolean contains(byte[] key);

    long size();

    void clear();

    default void putBatch(Set<Pair<byte[], byte[]>> batch) {
        for (Pair<byte[], byte[]> pair : batch) {
            this.put(pair.getLeft(), pair.getRight());
        }
    }

    @Override
    default void close() throws IOException {
    }
}
