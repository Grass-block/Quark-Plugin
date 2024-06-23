package org.tbstcraft.quark.framework.record;

import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.util.FilePath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class SimpleRecordEntry implements RecordEntry {
    public static final String FILE_LOCATION = "%s/record/%s.csv";
    public static final int MAX_SAVE_INTERVAL = 1000;

    private final String[] format;
    private final String id;

    private FileWriter writer;
    private long lastSaved;


    public SimpleRecordEntry(String id, String... recordFormat) {
        this.format = recordFormat;
        this.id = id;
    }

    public void open() {
        File f = new File(FILE_LOCATION.formatted(FilePath.pluginFolder(Quark.PLUGIN_ID), this.id));

        if (!f.exists() || f.length() == 0) {
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

            try (FileOutputStream stream = new FileOutputStream(f)) {
                for (int i = 0; i < this.format.length; i++) {
                    stream.write(this.format[i].getBytes(StandardCharsets.UTF_8));
                    if (i == this.format.length - 1) {
                        stream.write("\n".getBytes(StandardCharsets.UTF_8));
                        continue;
                    }
                    stream.write(",".getBytes(StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        try {
            this.writer = new FileWriter(f, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addLine(Object... components) {
        try {
            for (int i = 0; i < components.length; i++) {
                this.writer.write(components[i].toString());
                if (i == components.length - 1) {
                    this.writer.write("\n");
                    continue;
                }
                this.writer.write(",");
            }

            if (System.currentTimeMillis() - this.lastSaved > MAX_SAVE_INTERVAL) {
                this.lastSaved = System.currentTimeMillis();
                this.writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            this.writer.flush();
            this.writer.close();
        } catch (IOException e) {
            Quark.LOGGER.info("failed to close record entry %s: %s".formatted(this.id, e.getMessage()));
        }
    }
}
