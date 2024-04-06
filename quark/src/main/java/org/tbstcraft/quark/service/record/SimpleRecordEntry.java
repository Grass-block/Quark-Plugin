package org.tbstcraft.quark.service.record;

import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.SharedObjects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.Logger;

public final class SimpleRecordEntry implements RecordEntry {
    private final String format;
    private final Logger logger = Quark.LOGGER;
    private final String group;
    private final String id;
    private final String folder;
    private FileOutputStream stream;

    public SimpleRecordEntry(String folder, String group, String id, String recordFormat) {
        this.format = recordFormat;
        this.group = group;
        this.id = id;
        this.folder = folder;
    }

    private static File getFile(String folder, String group, String id) {
        String date = SharedObjects.DATE_FORMAT_FILE.format(new Date());
        String path = folder + "/%s/%s-%s.txt".formatted(group, id, date);
        File f = new File(path);
        if (f.getParentFile().mkdirs()) {
            Quark.LOGGER.info("created record folder of " + f.getName());
        }
        try {
            if (f.createNewFile()) {
                Quark.LOGGER.info("created record file " + f.getName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return f;
    }

    @Override
    public void record(String str, Object... format) {
        if (this.stream == null) {
            try {
                this.stream = new FileOutputStream(getFile(this.folder, this.group, this.id));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        String info = this.format.formatted(format);
        String date = SharedObjects.DATE_FORMAT.format(new Date());
        this.logger.info("[record] %s/%s: %s".formatted(this.group, this.id, info));
        try {
            this.stream.write("[%s] %s%n".formatted(date, info).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save() {
        if (this.stream == null) {
            return;
        }
        try {
            this.stream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        this.save();
        if (this.stream == null) {
            return;
        }
        try {
            this.stream.close();
        } catch (IOException e) {
            try {
                this.stream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
