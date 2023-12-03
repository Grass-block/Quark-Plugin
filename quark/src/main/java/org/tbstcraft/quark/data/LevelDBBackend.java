package org.tbstcraft.quark.data;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.tbstcraft.quark.util.nbt.NBTBuilder;
import org.tbstcraft.quark.util.nbt.NBTTagCompound;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class LevelDBBackend extends DataBackend {
    private DB db;

    public LevelDBBackend(File folder) {
        super(folder);
    }

    @Override
    public void open() {
        try {
            this.db = new Iq80DBFactory().open(this.getFolder(), new Options());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NBTTagCompound loadEntry(String id) {
        try {
            byte[] key = id.getBytes(StandardCharsets.UTF_8);
            byte[] data = this.db.get(key);
            if (data == null) {
                return new NBTTagCompound();
            }

            ByteArrayInputStream stream = new ByteArrayInputStream(data);
            GZIPInputStream zipInput = new GZIPInputStream(stream);
            NBTTagCompound tag = (NBTTagCompound) NBTBuilder.read(new DataInputStream(zipInput));
            zipInput.close();
            stream.close();

            return tag;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveEntry(String id, NBTTagCompound tag) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);
            GZIPOutputStream zipOutput = new GZIPOutputStream(stream);
            NBTBuilder.write(tag, new DataOutputStream(zipOutput));
            zipOutput.close();
            stream.close();

            byte[] data = stream.toByteArray();
            byte[] key = id.getBytes(StandardCharsets.UTF_8);

            this.db.put(key, data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            Class.forName("org.iq80.leveldb.util.ByteBufferSupport");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        super.close();
        try {
            this.db.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
