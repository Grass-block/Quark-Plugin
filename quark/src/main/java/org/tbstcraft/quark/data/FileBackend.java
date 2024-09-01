package org.tbstcraft.quark.data;

import me.gb2022.commons.nbt.NBT;
import me.gb2022.commons.nbt.NBTTagCompound;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.util.ExceptionUtil;

import java.io.*;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class FileBackend extends DataBackend {
    public FileBackend(File folder) {
        super(folder);
    }

    public String getDataFile(String id) {
        return this.getFolder().getAbsolutePath() + "/" + id;
    }

    public NBTTagCompound loadTag(String id) throws IOException {
        NBTTagCompound tag;
        FileInputStream fileInputStream = new FileInputStream(this.getDataFile(id));
        tag = (NBTTagCompound) NBT.readZipped(fileInputStream);
        fileInputStream.close();
        return tag;
    }

    public void overrideFile(File f, String id) {
        try {
            if (f.getParentFile().mkdirs()) {
                Quark.getInstance().getLogger().info("created data folder: " + this.getFolder().getName());
            }

            if (f.createNewFile()) {
                Quark.getInstance().getLogger().info("created data file: %s/%s".formatted(this.getFolder().getName(), id));
            }
            FileOutputStream fileOutputStream = new FileOutputStream(this.getDataFile(id));
            NBT.writeZipped(new NBTTagCompound(), fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int count() {
        return Objects.requireNonNull(this.getFolder().listFiles()).length;
    }

    @Override
    public NBTTagCompound load(String entryId) {
        NBTTagCompound tag;

        File f = new File(this.getDataFile(entryId));
        if (!f.exists() || f.length() == 0) {
            overrideFile(f, entryId);
        }

        try {
            tag = loadTag(entryId);
        } catch (Exception e) {
            overrideFile(f, entryId);
            try {
                tag = loadTag(entryId);
            } catch (Exception ex) {
                ExceptionUtil.log(e);
                return new NBTTagCompound();
            }
        }
        return tag;
    }

    @Override
    public void save(String entryId, NBTTagCompound tag) {
        try {
            NBT.writeZipped(tag, new ByteArrayOutputStream());
        }catch (Exception e){
            return;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(this.getDataFile(entryId))) {
            NBT.writeZipped(tag, fileOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void foreach(BiConsumer<String, NBTTagCompound> function) {
        for (File f : Objects.requireNonNull(this.getFolder().listFiles())) {
            try {
                function.accept(f.getName(), loadTag(f.getName()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
