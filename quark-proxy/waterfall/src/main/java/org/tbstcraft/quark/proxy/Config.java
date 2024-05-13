package org.tbstcraft.quark.proxy;

import com.moandjiezana.toml.Toml;

import java.io.*;
import java.util.Objects;

public interface Config {
    Toml CONFIG = new Toml();

    static void load() {
        File file = new File(QuarkProxy.INSTANCE.getDataFolder() + "/config.toml");
        if (!file.exists() || file.length() == 0) {
            restore();
        }
        try {
            CONFIG.read(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static void restore() {
        try {
            File file = new File(QuarkProxy.INSTANCE.getDataFolder() + "/config.toml");
            if (file.getParentFile().mkdirs()) {
                QuarkProxy.LOGGER.info("created config folder.");
            }
            if (file.createNewFile()) {
                QuarkProxy.LOGGER.info("created config file.");
            }
            try (InputStream in = Objects.requireNonNull(QuarkProxy.class.getResourceAsStream("/config.toml"));
                 OutputStream out = new FileOutputStream(file)) {
                out.write(in.readAllBytes());
                out.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Toml getSection(String id) {
        return CONFIG.getTable(id);
    }
}
