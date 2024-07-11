package org.tbstcraft.quark.data.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.util.FilePath;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class GlobalVars {
    private Plugin holder = Quark.PLUGIN;

    public Map<String, String> loadMap() {
        Map<String, String> map = new HashMap<>();

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
                return map;
            }

            for (String s : root.getKeys(false)) {
                if (!root.isConfigurationSection(s)) {
                    if (map.containsKey(s)) {
                        continue;
                    }
                    map.put(s, root.getString(s));
                    continue;
                }

                ConfigurationSection section = root.getConfigurationSection(s);

                assert section != null;

                for (String key2 : section.getKeys(false)) {
                    String id = s + ":" + key2;

                    if (!map.containsKey(key2)) {
                        map.put(key2, Objects.requireNonNull(section.get(key2)).toString());
                    }
                    if (!map.containsKey(id)) {
                        map.put(id, Objects.requireNonNull(section.get(key2)).toString());
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return map;
    }

    public void setHolder(Plugin holder) {
        this.holder = holder;
    }

    private File getExternalFile() {
        FilePath.tryReleaseAndGetFile("/templates/global_vars.yml", FilePath.pluginFolder(this.holder.getName()) + "/global_vars.yml");
        return new File(FilePath.pluginFolder(this.holder.getName()) + "/global_vars.yml");
    }

    public void restore() {
        FilePath.tryReleaseAndGetFile("/templates/global_vars.yml", FilePath.pluginFolder(this.holder.getName()) + "/global_vars.yml");
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
                YamlUtil.update(external, internal, sync, 3);
                external.save(f);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
