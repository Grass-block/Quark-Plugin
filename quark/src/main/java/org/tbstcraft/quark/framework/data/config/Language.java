package org.tbstcraft.quark.framework.data.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.data.language.ILanguageAccess;
import org.tbstcraft.quark.util.FilePath;
import org.tbstcraft.quark.util.Identifiers;
import org.tbstcraft.quark.util.platform.BukkitUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public final class Language extends ILanguageAccess {
    public static final String CHINESE_SIMPLIFIED = "zh_cn";
    public static final String[] ENABLED_LANGUAGES = new String[]{CHINESE_SIMPLIFIED};

    public static final String TEMPLATE_DIR = "/templates/lang/%s.%s.yml";
    private final HashMap<String, YamlConfiguration> sections = new HashMap<>();
    private final String id;
    private final String ownerId;
    private final Plugin owner;

    public Language(Plugin owner, String id) {
        this.owner = owner;
        this.ownerId = "quark";
        ConfigDelegation.LANGUAGE_REGISTRY.put(id, this);
        this.id = id;
        this.reload();
        this.sync(false);
    }

    public static Language create(String id) {
        return new Language(Quark.PLUGIN, id);
    }

    public static Language create(Plugin owner, String id) {
        return new Language(owner, id);
    }

    public static String handleReplacement(String src, ConfigurationSection dataSrc, String replacementPrefix) {
        Pattern replacement = Pattern.compile("\\{%s#(.*?)\\}".formatted(replacementPrefix));
        Matcher matcher2 = replacement.matcher(src);
        while (matcher2.find()) {
            String s = matcher2.group();
            String tagName = s.substring(replacementPrefix.length() + 2, s.length() - 1);
            if (!dataSrc.contains(tagName)) {
                continue;
            }
            if (dataSrc.isList(tagName)) {
                List<String> lst = dataSrc.getStringList(tagName);
                src = src.replace(s, lst.get(new Random().nextInt(lst.size())));
                continue;
            }
            src = src.replace(s, Objects.requireNonNull(dataSrc.getString(tagName)));
        }
        return src;
    }

    //ui build
    private static String generateMessage(ConfigurationSection section, String id) {
        id = Identifiers.external(id);
        if (section.isString(id)) {
            return section.getString(id);
        }

        List<String> list = section.getStringList(id);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String s2 : list) {
            i++;
            sb.append(s2);
            if (i < list.size()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }


    public void sync(boolean clean) {
        try {
            for (String locale : this.sections.keySet()) {
                YamlConfiguration cfg = new YamlConfiguration();
                String srcDir = TEMPLATE_DIR.formatted(this.id, locale);
                InputStream is = this.owner.getClass().getResourceAsStream(srcDir);
                YamlUtil.loadUTF8(cfg, is);
                YamlUtil.update(this.sections.get(locale), cfg, clean, 3);

                String file = "%s/lang/%s/%s.yml".formatted(FilePath.pluginFolder(this.ownerId), BukkitUtil.fixLocaleId(locale), this.id);
                try {
                    this.sections.get(locale).save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            Quark.LOGGER.warning("failed to sync language %s: %s".formatted(this.id, e.getMessage()));
        }
    }


    public void restore() {
        for (String id : ENABLED_LANGUAGES) {
            String fileDir = "%s/lang/%s/%s.yml".formatted(FilePath.pluginFolder(this.ownerId), BukkitUtil.fixLocaleId(id), this.id);
            String srcDir = TEMPLATE_DIR.formatted(this.id, BukkitUtil.fixLocaleId(id));
            FilePath.coverFile(srcDir, fileDir);
            this.load(id, srcDir, fileDir);
        }
    }


    public void reload() {
        for (String id : ENABLED_LANGUAGES) {
            String fileDir = "%s/lang/%s/%s.yml".formatted(FilePath.pluginFolder(this.ownerId), BukkitUtil.fixLocaleId(id), this.id);
            String srcDir = "/lang/%s.%s.yml".formatted(this.id, BukkitUtil.fixLocaleId(id));
            this.load(id, srcDir, fileDir);
        }
    }

    private void load(String id, String srcDir, String fileDir) {
        File file = FilePath.tryReleaseAndGetFile(srcDir, fileDir);
        if (!file.exists()) {
            return;
        }
        try {
            YamlConfiguration config = new YamlConfiguration();
            YamlUtil.loadUTF8(config, new FileInputStream(file));
            this.sections.put(BukkitUtil.fixLocaleId(id), config);
        } catch (Exception e) {
            this.restore();
            this.load(id, srcDir, fileDir);
        }
    }


    //config access
    private String getMessageFromConfig(Locale locale, String namespace, String id) {
        id = Identifiers.external(id);
        namespace = Identifiers.external(namespace);

        ConfigurationSection section = this.getNameSpace(locale, namespace);
        if (section == null) {
            section = this.getNameSpace(Locale.CHINA, namespace);
        }
        if (section == null) {
            return "ERROR: SECTION_NOT_FOUND(zh_cn/%s)".formatted(namespace);
        }
        if (!section.contains(id)) {
            return "ERROR: MESSAGE_NOT_FOUND(zh_cn/%s)".formatted(id);
        }
        if (section.isString(id)) {
            return section.getString(id);
        }
        return generateMessage(section, id);
    }

    @SuppressWarnings("unchecked")
    private List<String> getMessageListFromConfig(Locale locale, String namespace, String id) {
        id = Identifiers.external(id);
        namespace = Identifiers.external(namespace);
        ConfigurationSection section = this.getNameSpace(locale, namespace);
        if (section == null) {
            return List.of("ERROR: SECTION_NOT_FOUND(%s)".formatted(namespace));
        }
        if (!section.contains(id)) {
            return List.of("ERROR: MESSAGE_NOT_FOUND(%s)".formatted(id));
        }

        List<?> list = section.getList(id);
        if (list == null) {
            return List.of("ERROR: MESSAGE_NOT_FOUND(%s)".formatted(id));
        }

        if (list.get(0) instanceof String) {
            return section.getStringList(id);
        }

        List<String> lists = new ArrayList<>();

        for (List<String> block : ((List<List<String>>) list)) {
            int i = 0;
            StringBuilder sb = new StringBuilder();
            for (String s2 : block) {
                i++;
                sb.append(s2);
                if (i <= list.size()) {
                    sb.append("\n");
                }
            }
            lists.add(sb.toString());
        }
        return lists;
    }

    private ConfigurationSection getNameSpace(Locale locale, String namespace) {
        namespace = Identifiers.external(namespace);
        ConfigurationSection root = this.getRootSection(locale);
        if (root == null) {
            root = this.getRootSection(Locale.CHINA);
        }
        if (root == null) {
            return null;
        }
        return root.getConfigurationSection(namespace);
    }


    private ConfigurationSection getRootSection(Locale locale) {
        ConfigurationSection section = this.sections.get(org.tbstcraft.quark.framework.data.language.Language.locale(locale));
        if (section == null) {
            return null;
        }
        return section.getConfigurationSection("language");
    }

    @Override
    public String getRawMessage(Locale locale, String namespace, String id) {
        return this.getMessageFromConfig(locale, namespace, id);
    }

    @Override
    public List<String> getRawMessageList(Locale locale, String namespace, String id) {
        return getMessageListFromConfig(locale, namespace, id);
    }

    @Override
    public boolean hasKey(String namespace, String id) {
        if (!this.hasNamespace(namespace)) {
            return false;
        }
        return Objects.requireNonNull(this.getNameSpace(Locale.CHINA, namespace)).contains(id);
    }

    @Override
    public boolean hasNamespace(String namespace) {
        return this.getNameSpace(Locale.CHINA, namespace) != null;
    }
}
