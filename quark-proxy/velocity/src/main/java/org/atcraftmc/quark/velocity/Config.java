package org.atcraftmc.quark.velocity;

import com.moandjiezana.toml.Toml;

import java.io.*;
import java.util.Objects;

public final class Config {
    private final Toml dom = new Toml();
    private final QuarkVelocity plugin;

    public Config(QuarkVelocity plugin) {
        this.plugin = plugin;
    }

    public void load() {
        try {
            File file = new File(this.plugin.getDataDirectory() + ("/config.toml"));
            System.out.println(file.getAbsolutePath());
            if (!file.exists() || file.length() == 0) {
                if (file.getParentFile().mkdirs()) {
                    this.plugin.getLogger().info("created config folder.");
                }
                if (file.createNewFile()) {
                    this.plugin.getLogger().info("created config file.");
                }

                try (InputStream in = Objects.requireNonNull(QuarkVelocity.class.getResourceAsStream("/config.toml"));
                     OutputStream out = new FileOutputStream(file)) {
                    out.write(in.readAllBytes());
                    out.flush();
                }
            }
            this.dom.read(new FileInputStream(file));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Toml entry(String id) {
        return this.dom.getTable(id);
    }


    public QuarkVelocity getPlugin() {
        return plugin;
    }

    public Toml getDom() {
        return dom;
    }
}
