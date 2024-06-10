package org.tbstcraft.quark.framework.data.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.util.FilePath;
import org.tbstcraft.quark.util.query.TemplateEngine;

import java.io.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class GlobalVars {
    private final Set<String> keys = new HashSet<>();

    private final TemplateEngine target;
    private Plugin holder;

    public GlobalVars(TemplateEngine target) {
        this.target = target;
    }

    public void setHolder(Plugin holder) {
        this.holder = holder;
    }

    private File getExternalFile() {
        return new File(FilePath.pluginFolder(this.holder.getName()) + "/global_vars.yml");
    }

    public void load() {
        try {
            File f = this.getExternalFile();
            this._update(false, f);
            YamlConfiguration external = new YamlConfiguration();
            try {
                external.load(f);
            } catch (IOException | InvalidConfigurationException e) {
                restore();
                external.load(f);
            }


            ConfigurationSection root = external.getConfigurationSection("global-vars");
            if (root == null) {
                return;
            }

            for (String s : this.keys) {
                this.target.unregister(s);
            }
            this.keys.clear();

            for (String s : root.getKeys(false)) {
                if (!root.isConfigurationSection(s)) {
                    if (this.keys.contains(s)) {
                        continue;
                    }
                    this.target.register(s, () -> root.get(s));
                    this.keys.add(s);
                    continue;
                }

                ConfigurationSection section = root.getConfigurationSection(s);

                assert section != null;

                for (String key2 : section.getKeys(false)) {
                    String id = s + ":" + key2;

                    if (!this.keys.contains(key2)) {
                        this.target.register(key2, () -> section.get(key2));
                        this.keys.add(key2);
                    }
                    if (!this.keys.contains(id)) {
                        this.target.register(id, () -> section.get(key2));
                        this.keys.add(id);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void restore() {
        FilePath.tryReleaseAndGetFile("/templates/global_vars.yml", FilePath.pluginFolder(this.holder.getName()) + "/global_vars.yml");
        this.load();
    }

    public void sync() {
        File f = this.getExternalFile();
        this._update(true, f);
    }

    private void _update(boolean sync, File f) {
        YamlConfiguration external = new YamlConfiguration();
        try {
            if (!f.exists() || f.length() == 0) {
                this.restore();
                external.load(f);
            } else {
                external.load(f);
                YamlConfiguration internal = new YamlConfiguration();

                InputStream stream = this.holder.getClass().getResourceAsStream("/templates/global_vars.yml");
                Reader reader = new InputStreamReader(Objects.requireNonNull(stream));
                internal.load(reader);
                YamlUtil.update(external, internal, sync, 2);
                external.save(f);
            }
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
