package org.tbstcraft.quark.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.util.FilePath;
import org.tbstcraft.quark.util.query.TemplateEngine;
import org.tbstcraft.quark.util.query.ValueSupplier;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class GlobalVars {
    private final Map<String, String> map = new HashMap<>();
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
            external.load(f);
            ConfigurationSection section = external.getConfigurationSection("global-vars");
            if (section == null) {
                return;
            }
            for (String s : this.map.keySet()) {
                this.target.unregister(s);
            }
            this.map.clear();
            for (String s : section.getKeys(false)) {
                this.map.put(s, section.getString(s));
                this.target.register(s, new ValueSupplier(section.getString(s)));
            }
        } catch (IOException | InvalidConfigurationException e) {
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
