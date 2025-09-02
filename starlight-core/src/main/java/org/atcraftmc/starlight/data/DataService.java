package org.atcraftmc.starlight.data;

import me.gb2022.commons.math.SHA;
import me.gb2022.commons.nbt.NBT;
import me.gb2022.commons.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.atcraftmc.starlight.data.storage.backend.DataStorage;
import org.atcraftmc.starlight.data.storage.DataEntry;
import org.atcraftmc.starlight.data.storage.StorageContext;
import org.atcraftmc.starlight.util.Identifiers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;

public class DataService implements StorageContext {
    public static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("DataService");
    protected final Logger logger = Logger.getLogger("DataService");
    private final ArrayDeque<String> saveRequest = new ArrayDeque<>();
    private final HashMap<String, NBTTagCompound> cache = new HashMap<>();
    private final HashMap<String, DataEntry> entries = new HashMap<>();
    private final File folder;
    private final boolean available = true;
    private DataStorage storage;
    private DataBackend backend;

    public DataService(File folder) {
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + folder.getAbsolutePath());
        }

        this.folder = folder;
    }

    @Override
    public void save(DataEntry entry) {
        this.saveEntry(entry.getId());
    }

    public synchronized DataEntry get(String id) {
        return this.entries.computeIfAbsent(id, k -> new DataEntry(this.getEntry(id), this, k));
    }

    public synchronized void open() {
        this.storage = DataStorage.detect(this.folder);
    }

    public synchronized void close() {
        for (var id : this.cache.keySet()) {
            this.saveEntry(id);
        }
        this.cache.clear();

        try {
            this.storage.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    public File getFile(String id) {
        return new File(((FileBackend) this.backend).getDataFile(hash(id)));
    }

    public void clear() {
        for (File f : Objects.requireNonNull(this.getFolder().listFiles())) {
            if (f.delete()) {
                continue;
            }
            this.logger.severe("cannot delete: " + f.getName());
        }
    }

    public File getFolder() {
        return folder;
    }

    public DataBackend getBackend() {
        return backend;
    }

    String hash(String id) {
        return SHA.getSHA1(id, false);
    }

    public int getEntryCount() {
        return this.getBackend().count();
    }

    //direct
    public synchronized NBTTagCompound getEntry(String id) {
        id = Identifiers.internal(id);
        if (!this.cache.containsKey(id)) {
            this.cache.put(id, this.read(id.getBytes(StandardCharsets.UTF_8)));
        }
        return this.cache.get(id);
    }

    public synchronized void saveEntry(String id) {
        id = Identifiers.internal(id);

        if (!this.cache.containsKey(id)) {
            return;
        }
        this.write(id.getBytes(StandardCharsets.UTF_8), this.cache.get(id));
    }

    public synchronized void write(byte[] key, NBTTagCompound tag) {
        var write = new ByteArrayOutputStream(128);

        try {
            NBT.writeZipped(tag, write);
        } catch (Exception e) {
            LOGGER.error("failed when serializing entry {}, it *might* be corrupted.", new String(key, StandardCharsets.UTF_8));
            LOGGER.catching(e);
            return;
        }

        try {
            write.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            this.storage.put(key, write.toByteArray());
        } catch (Exception e) {
            LOGGER.error("failed when writing entry {}, it *might* be corrupted.", new String(key, StandardCharsets.UTF_8));
            LOGGER.catching(e);
        }
    }

    public synchronized NBTTagCompound read(byte[] key) {
        byte[] data;

        try {
            data = this.storage.get(key);
        } catch (Exception e) {
            LOGGER.error("failed when reading entry {}, it *might* be corrupted.", new String(key, StandardCharsets.UTF_8));
            LOGGER.catching(e);
            return new NBTTagCompound();
        }

        if (data == null) {
            LOGGER.info("creating empty entry {}.", new String(key, StandardCharsets.UTF_8));
            return new NBTTagCompound();
        }

        try (var read = new ByteArrayInputStream(data)) {
            return ((NBTTagCompound) NBT.readZipped(read));
        } catch (IOException e) {
            LOGGER.error("failed when deserializing entry {}, it *might* be corrupted.", new String(key, StandardCharsets.UTF_8));
            LOGGER.catching(e);
            return new NBTTagCompound();
        }
    }
}
