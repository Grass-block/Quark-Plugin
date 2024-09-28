package org.tbstcraft.quark.data;

import me.gb2022.commons.math.SHA;
import me.gb2022.commons.nbt.NBT;
import me.gb2022.commons.nbt.NBTTagCompound;
import org.tbstcraft.quark.data.storage.DataEntry;
import org.tbstcraft.quark.data.storage.StorageContext;
import org.tbstcraft.quark.util.Identifiers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class DataService implements StorageContext {
    private final ArrayDeque<String> saveRequest = new ArrayDeque<>();
    private final HashMap<String, NBTTagCompound> cache = new HashMap<>();
    private final HashMap<String, DataEntry> entries = new HashMap<>();
    private final Logger logger;
    private final File folder;
    private DataBackend backend;
    private boolean available = true;

    public DataService(Logger logger, File folder) {
        this.logger = logger;
        this.folder = folder;
    }


    @Override
    public void save(DataEntry entry) {
        this.saveEntry(entry.getId());
    }

    public DataEntry get(String id) {
        return this.entries.computeIfAbsent(id, k -> new DataEntry(this.getEntry(id), this, k));
    }

    //convert
    public void convertToDB() {
        if (isLevelDBStorage()) {
            throw new RuntimeException("not a File backend!");
        }

        this.convertTo(new LevelDBBackend(this.getFolder()));
    }

    public void covertToLegacy() {
        if (!isLevelDBStorage()) {
            throw new RuntimeException("not a LevelDB backend!");
        }

        this.convertTo(new FileBackend(this.getFolder()));
    }

    private void convertTo(DataBackend backend) {
        this.available = false;

        this.close();

        this.backend.open();

        Map<String, NBTTagCompound> cache1 = new HashMap<>();

        this.backend.foreach(cache1::put);
        this.backend.close();

        for (File f : Objects.requireNonNull(this.getFolder().listFiles())) {
            f.delete();
        }

        Map<String, NBTTagCompound> cache = cache1;

        this.backend = backend;
        this.backend.open();

        for (String id : cache.keySet()) {
            NBTTagCompound tag = this.cache.get(id);
            try {
                NBT.write(tag, new ByteArrayOutputStream());
            } catch (Exception ignored) {
                continue;
            }

            this.backend.save(id, tag);
        }

        this.backend.close();
        this.backend.open();

        this.available = true;
    }


    public synchronized void open() {
        if (this.isLevelDBStorage()) {
            this.backend = new LevelDBBackend(this.getFolder());
        } else {
            this.backend = new FileBackend(this.getFolder());
        }
        this.backend.open();
    }

    public synchronized void close() {
        for (String id : this.cache.keySet()) {
            this.saveEntry(id);
        }
        this.cache.clear();
        this.backend.close();
    }

    public synchronized NBTTagCompound getEntry(String id) {
        id = Identifiers.internal(id);
        if (!this.cache.containsKey(id)) {
            this.cache.put(id, this.backend.load(hash(id)));
        }
        return this.cache.get(id);
    }

    public synchronized void saveEntry(String id) {
        id = Identifiers.internal(id);
        if (!this.available) {
            if (!this.saveRequest.contains(id)) {
                this.saveRequest.add(id);
            }
        }
        this.batchSaveRequest();
        if (!this.cache.containsKey(id)) {
            return;
        }
        this.backend.save(hash(id), this.cache.get(id));
    }

    public synchronized void batchSaveRequest() {
        while (!this.saveRequest.isEmpty()) {
            this.saveEntry(this.saveRequest.poll());
        }
    }


    public void rollback(File backup) {

    }

    public void backup(File backup) {

    }


    public void clear() {
        for (File f : Objects.requireNonNull(this.getFolder().listFiles())) {
            if (f.delete()) {
                continue;
            }
            this.logger.severe("cannot delete: " + f.getName());
        }
    }

    public boolean isLevelDBStorage() {
        File folder1 = getFolder();
        folder1.mkdirs();
        File[] files = folder1.listFiles();
        if (files == null) {
            throw new RuntimeException("invalid data storage!");
        }
        for (File file : files) {
            if (file.isFile() && file.getName().contains("CURRENT")) {
                return true;
            }
        }
        return false;
    }

    public File getFolder() {
        return folder;
    }

    public DataBackend getBackend() {
        return backend;
    }

    private String hash(String id) {
        return SHA.getSHA1(id, false);
    }

    public int getEntryCount() {
        return this.getBackend().count();
    }
}
