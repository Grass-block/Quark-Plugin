package org.tbstcraft.quark.data;

import org.tbstcraft.quark.util.BukkitUtil;
import org.tbstcraft.quark.util.nbt.NBTBuilder;
import org.tbstcraft.quark.util.nbt.NBTTagCompound;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileBackend extends DataBackend {
    public FileBackend(File folder) {
        super(folder);
    }

    @Override
    public NBTTagCompound loadEntry(String id) {
        NBTTagCompound tag;
        File f = new File(this.getPathPrefix() + BukkitUtil.encrypt(id));
        if (!f.exists() || f.length() == 0) {
            try {
                f.getParentFile().mkdirs();
                f.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(this.getPathPrefix() + BukkitUtil.encrypt(id));
                GZIPOutputStream zip = new GZIPOutputStream(fileOutputStream);
                DataOutputStream dataOutputStream = new DataOutputStream(zip);

                NBTBuilder.write(new NBTTagCompound(), dataOutputStream);

                dataOutputStream.close();
                zip.close();
                fileOutputStream.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(this.getPathPrefix() + BukkitUtil.encrypt(id));
            GZIPInputStream zip = new GZIPInputStream(fileInputStream);
            DataInputStream dataInputStream = new DataInputStream(zip);

            tag = (NBTTagCompound) NBTBuilder.read(dataInputStream);

            dataInputStream.close();
            zip.close();
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tag;
    }

    @Override
    public void saveEntry(String id, NBTTagCompound tag) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(this.getPathPrefix() + BukkitUtil.encrypt(id));
            GZIPOutputStream zip = new GZIPOutputStream(fileOutputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(zip);

            NBTBuilder.write(tag, dataOutputStream);

            dataOutputStream.close();
            zip.close();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPathPrefix() {
        return this.getFolder().getAbsolutePath() + "/";
    }
}
