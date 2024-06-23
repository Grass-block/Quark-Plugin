package org.tbstcraft.quark.data.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public interface YamlUtil {
    static void update(ConfigurationSection target, ConfigurationSection template, boolean deleteUnused, int keyLayer) {
        if (!needIterationCheck(target, template, keyLayer)) {
            return;
        }
        _updateSection(target, template, deleteUnused, keyLayer, 1);
    }

    static boolean needIterationCheck(ConfigurationSection target, ConfigurationSection template, int keyLayer) {
        List<String> targetKeys = new ArrayList<>(scanKeys(target, keyLayer + 1));
        List<String> templateKeys = new ArrayList<>(scanKeys(template, keyLayer + 1));

        if (targetKeys.size() != templateKeys.size()) {
            return true;
        }
        for (String s : targetKeys) {
            if (!templateKeys.contains(s)) {
                return true;
            }
        }

        return false;
    }

    static void _updateSection(ConfigurationSection target, ConfigurationSection template, boolean deleteUnused, int keyLayer, int nowLayer) {
        if (target == null || template == null) {
            return;
        }
        injectNew(target, template);
        if (deleteUnused) {
            deleteUnused(target, template);
        }

        if (nowLayer == keyLayer) {
            return;
        }
        for (String key : template.getKeys(false)) {
            ConfigurationSection tgt = target.getConfigurationSection(key);
            ConfigurationSection repl = template.getConfigurationSection(key);
            if (repl == null || tgt == null) {
                continue;
            }
            _updateSection(tgt, repl, deleteUnused, keyLayer, nowLayer + 1);
        }
    }

    static void deleteUnused(ConfigurationSection target, ConfigurationSection template) {
        for (String key : target.getKeys(false)) {
            if (template.contains(key)) {
                continue;
            }
            target.set(key, null);
        }
    }

    static void injectNew(ConfigurationSection target, ConfigurationSection template) {
        for (String key : template.getKeys(false)) {
            if (target.getKeys(false).contains(key)) {
                continue;
            }
            target.set(key, template.get(key));
        }
    }


    static List<String> scanKeys(ConfigurationSection target, int keyLayer) {
        List<String> list = new ArrayList<>(128);
        _scanKeys(target, list, keyLayer, 1);
        return list;
    }

    static void _scanKeys(ConfigurationSection target, List<String> keys, int keyLayer, int nowLayer) {
        if (nowLayer == keyLayer) {
            return;
        }
        for (String key : target.getKeys(false)) {
            keys.add(key);
            ConfigurationSection tgt = target.getConfigurationSection(key);
            if (tgt == null) {
                continue;
            }
            _scanKeys(tgt, keys, keyLayer, nowLayer + 1);
        }
    }


    static void loadUTF8(YamlConfiguration cfg, InputStream stream) {
        try {
            String str = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            str = Queries.applyEnvironmentVars(str);
            cfg.loadFromString(str);
            stream.close();
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    static void saveUTF8(YamlConfiguration cfg, OutputStream stream) {
        try {
            stream.write(cfg.saveToString().getBytes(StandardCharsets.UTF_8));
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
