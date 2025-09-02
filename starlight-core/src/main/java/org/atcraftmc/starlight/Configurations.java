package org.atcraftmc.starlight;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.PluginConcept;
import org.atcraftmc.qlib.config.Configuration;
import org.atcraftmc.qlib.config.YamlUtil;
import org.atcraftmc.starlight.util.FilePath;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public interface Configurations {
    Logger LOGGER = LogManager.getLogger("Starlight/Config");
    String RES_TEMPLATE = "/templates/%s.%s";
    String FILE_DIR = "%s/config/%s";
    String FILE_TEMPLATE = "%s/config/%s/template.%s";
    String FILE_CUSTOM = "%s/config/%s/%s";

    static ConfigurationSection values(String template, String file) {
        var f = FilePath.tryReleaseAndGetFile(template, file);

        var configDOM = YamlConfiguration.loadConfiguration(f);
        var templateDOM = YamlConfiguration.loadConfiguration(new InputStreamReader(FilePath.getPluginResource(template)));

        YamlUtil.update(configDOM, templateDOM, false, 2);

        try {
            configDOM.save(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return configDOM;
    }

    //file, dom[root]
    static Map<String, String> grouped(String name, Set<String> defaults, String commentPrefix, String extensionName) {
        var result = new HashMap<String, String>();
        var directoryFile = new File(FILE_DIR.formatted(FilePath.slDataFolder(), name));
        var templateFile = new File(FILE_TEMPLATE.formatted(FilePath.slDataFolder(), name, extensionName));

        if (!directoryFile.exists()) {
            if (directoryFile.mkdirs()) {
                LOGGER.info("created grouped template dir {}", directoryFile);
            }
        }

        for (var d : defaults) {
            var in = FilePath.getPluginResource("/templates/" + d);
            var lastIdxSeparatorInName = d.lastIndexOf("/");
            var justName = lastIdxSeparatorInName == -1 ? d : d.substring(lastIdxSeparatorInName);

            var df = new File(FILE_CUSTOM.formatted(FilePath.slDataFolder(), name, justName));

            if (in != null) {
                try {
                    if (df.createNewFile()) {
                        LOGGER.info("created grouped defaults {}", df);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try (var o = new FileOutputStream(df)) {
                    o.write((commentPrefix + " This file is for example/defaults of this group.\n").getBytes(StandardCharsets.UTF_8));
                    o.write((commentPrefix + " It is auto saved by Starlight plugin.\n").getBytes(StandardCharsets.UTF_8));
                    o.write((commentPrefix + " You COULD rather edit or clear this file.\n").getBytes(StandardCharsets.UTF_8));
                    o.write((commentPrefix + " To make this file re-appear, just delete it.\n").getBytes(
                            StandardCharsets.UTF_8));
                    o.write(in.readAllBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }


        var in = FilePath.getPluginResource(RES_TEMPLATE.formatted(name, extensionName));

        if (in != null) {
            try {
                if (templateFile.createNewFile()) {
                    LOGGER.info("created grouped template file {}.", templateFile);
                }
            } catch (IOException e) {
                LOGGER.error("failed to create file: {}", templateFile);
                LOGGER.catching(e);
            }

            try (var o = new FileOutputStream(templateFile)) {
                o.write((commentPrefix + " This file is for template for all config entries in the current group/folder.\n").getBytes(
                        StandardCharsets.UTF_8));
                o.write((commentPrefix + " // Do NOT edit any files in it.\n").getBytes(StandardCharsets.UTF_8));
                o.write((commentPrefix + " // Consider referring to this template to create more entries.\n").getBytes(StandardCharsets.UTF_8));
                o.write(in.readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        var files = directoryFile.listFiles();

        for (var f : Objects.requireNonNull(files)) {
            if (f.getName().startsWith("template.")) {
                continue;
            }
            if (!f.getName().endsWith(extensionName)) {
                continue;
            }

            try (var i = new FileInputStream(f)) {
                var s = new String(i.readAllBytes());
                result.put(f.getName(), removeComments(s, commentPrefix));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    static Map<String, JsonObject> groupedJson(String name, Set<String> defaults) {
        defaults = new HashSet<>(defaults);
        defaults.add(name + ".default.json");
        var result = new HashMap<String, JsonObject>();
        grouped(name, defaults, "//", "json").forEach((k, v) -> result.put(k, JsonParser.parseString(v).getAsJsonObject()));
        return result;
    }

    static Map<String, ConfigurationSection> groupedYML(String name, Set<String> defaults) {
        defaults = new HashSet<>(defaults);
        defaults.add(name + ".default.yml");
        var result = new HashMap<String, ConfigurationSection>();
        grouped(name, defaults, "#", "yml").forEach((k, v) -> {
            var c = new YamlConfiguration();
            try {
                c.loadFromString(v);
            } catch (InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
            result.put(k, c.getConfigurationSection(name));
        });
        return result;
    }

    static String removeComments(String json, String cpf) {
        var sb = new StringBuilder();
        for (var s : json.split("\n")) {
            sb.append(s.split(cpf)[0]).append("\n");
        }

        return sb.toString();
    }

    static ConfigurationSection standalone(String name) {
        var file = "%s/%s.yml".formatted(FilePath.slDataFolder(), name);
        var template = "/templates/%s.yml".formatted(name);

        return values(template, file).getConfigurationSection(name);
    }

    static File file(String name, boolean cover) {
        var file = "%s/config/%s".formatted(FilePath.slDataFolder(), name);
        var template = "/templates/%s".formatted(name);
        var f = new File(file);

        if (!f.exists()) {
            if (f.getParentFile().mkdirs()) {
                LOGGER.info("created standalone file {}.", file);
            }
            try {
                if (!f.createNewFile()) {
                    LOGGER.error("failed to create standalone file {}.", file);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (f.length() == 0 || cover) {
            try (var o = new FileOutputStream(f)) {
                o.write(FilePath.getPluginResource(template).readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return f;
    }

    static ConfigurationSection secret(String name) {
        var file = "%s/secret/%s.yml".formatted(FilePath.slDataFolder(), name);
        var template = "/templates/secret/%s.yml".formatted(name);

        return values(template, file).getConfigurationSection(name);
    }

    static Configuration values(PluginConcept provider, String id) {
        return new SLConfigurationPack(provider, id);
    }

    final class SLConfigurationPack extends Configuration {
        public SLConfigurationPack(PluginConcept provider, String id) {
            super(provider, id);
        }

        @Override
        public String getStorageFile() {
            return "/config/values/%s.yml".formatted(this.id);
        }

        @Override
        public String getTemplateFile() {
            return "/config/values/%s.template.yml".formatted(this.id);
        }
    }
}
