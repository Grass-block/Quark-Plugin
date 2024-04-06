package org.tbstcraft.quark.proxy;

import net.md_5.bungee.api.plugin.Plugin;
import org.tbstcraft.quark.proxy.modulepeer.BungeeConnectionProtect;
import org.tbstcraft.quark.proxy.syncs.KickSync;
import org.tbstcraft.quark.proxy.syncs.PingSync;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

public final class QuarkProxy extends Plugin {
    public static final Sync[] SYNCS = new Sync[]{
            new PingSync(),
            new KickSync(),
            new BungeeConnectionProtect(),
    };
    public static QuarkProxy INSTANCE = null;

    private final Properties config = new Properties();

    @Override
    public void onEnable() {
        INSTANCE = this;
        this.loadConfig();
        for (Sync s : SYNCS) {
            s.init(this, this.getProxy());
            s.onEnable();
        }
        HubCommand.register(this);
    }

    private void loadConfig() {
        try {
            File file = new File(getDataFolder() + "/config.properties");
            if (!file.exists() || file.length() == 0) {
                file.getParentFile().mkdirs();
                if (file.createNewFile()) {
                    this.getLogger().info("created config file.");
                }
                try (InputStream in = Objects.requireNonNull(this.getClass().getResourceAsStream("/config.properties"));
                     OutputStream out = new FileOutputStream(file)) {
                    out.write(in.readAllBytes());
                    out.flush();
                }

            }
            this.config.load(new FileInputStream(file));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Properties getConfig() {
        return config;
    }
}

