package org.tbstcraft.quark.service.data;

import me.gb2022.commons.nbt.NBT;
import me.gb2022.commons.nbt.NBTTagCompound;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.tbstcraft.quark.util.ExceptionUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public final class LevelDBBackend extends DataBackend {
    private DB db;

    public LevelDBBackend(File folder) {
        super(folder);
    }

    @Override
    public void onOpen() {
        try {
            this.db = new Iq80DBFactory().open(this.getFolder(), new Options());
            System.out.println("levelDB opened");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClose() {
        try {
            this.db.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int count() {
        AtomicInteger i = new AtomicInteger();
        this.db.forEach((a) -> i.getAndIncrement());
        return i.get();
    }

    @Override
    public NBTTagCompound load(String entryId) {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(this.db.get(entryId.getBytes(StandardCharsets.UTF_8)))) {
            return (NBTTagCompound) NBT.readZipped(stream);
        } catch (Exception e) {
            ExceptionUtil.log(e);
            return new NBTTagCompound();
        }
    }

    @Override
    public void save(String entryId, NBTTagCompound tag) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            NBT.writeZipped(tag, stream);
            this.db.put(entryId.getBytes(StandardCharsets.UTF_8), stream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void foreach(BiConsumer<String, NBTTagCompound> function) {
        this.db.forEach(entry -> {
            try (ByteArrayInputStream stream = new ByteArrayInputStream(entry.getValue())) {
                String key = new String(entry.getKey());
                NBTTagCompound value = (NBTTagCompound) NBT.readZipped(stream);

                if (value == null) {
                    return;
                }

                function.accept(key, value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
