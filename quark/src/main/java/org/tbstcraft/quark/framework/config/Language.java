package org.tbstcraft.quark.framework.config;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.text.TextBuilder;
import org.tbstcraft.quark.framework.text.TextSender;
import org.tbstcraft.quark.util.FilePath;
import org.tbstcraft.quark.util.Identifiers;
import org.tbstcraft.quark.util.api.BukkitUtil;
import org.tbstcraft.quark.util.api.PlayerUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public interface Language {
    Pattern MESSAGE_PATTERN = Pattern.compile("\\{msg#(.*?)\\}");
    Pattern RANDOM_MESSAGE_PATTERN = Pattern.compile("\\{rand#(.*?)\\}");
    Pattern LOCALIZED_GLOBAL_VAR = Pattern.compile("\\{global#(.*?)\\}");
    String CHINESE_SIMPLIFIED = "zh_cn";
    String[] ENABLED_LANGUAGES = new String[]{CHINESE_SIMPLIFIED};


    @SuppressWarnings({"unused"})
    String ENGLISH = "en";

    @SuppressWarnings({"unused"})
    String FRENCH = "fr";

    @SuppressWarnings({"unused"})
    String JAPANESE = "jp";

    @SuppressWarnings({"unused"})
    String RUSSIA = "ru";

    static Language create(String id) {
        return new Impl(Quark.PLUGIN, id);
    }

    static Language create(Plugin owner, String id) {
        return new Impl(owner, id);
    }

    static String getLocale(CommandSender sender) {
        return sender instanceof Player ? PlayerUtil.getLocale(((Player) sender)) : "en_us";
    }

    static String handleReplacement(String src, ConfigurationSection dataSrc, String replacementPrefix) {
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


    static String toId(Locale locale) {
        if (locale == Locale.SIMPLIFIED_CHINESE) {
            return "zh_cn";
        }
        if (locale == Locale.ENGLISH) {
            return "en";
        }
        return "";
        //todo: IMPORTANT more id support
    }

    static Locale toLocale(String id) {
        return Locale.SIMPLIFIED_CHINESE;//todo
    }

    static String format(String s, Object... format) {
        for (int i = 0; i < format.length; i++) {
            s = s.replace("{" + i + "}", format[i].toString());
        }
        return Queries.GLOBAL_TEMPLATE_ENGINE.handle(s);
    }

    static Locale locale(CommandSender sender) {
        return toLocale(getLocale(sender));
    }

    void sync(boolean clean);

    void restore();

    void reload();

    LanguageEntry createEntry(String moduleId);

    String buildUI(ConfigurationSection parentSection, String id, String module, String locale, Function<String, String> sourceProcessor);

    String buildUI(String source, String module, String locale);

    String getMessage(String locale, String moduleId, String messageId, Object... format);

    String getRandomMessage(String locale, String moduleId, String msg, Object... format);

    List<String> getMessageList(String locale, String moduleId, String messageId);

    void sendMessageTo(CommandSender target, String moduleId, String msg, Object... format);

    void broadcastMessage(boolean opOnly, String moduleId, String msg, Object... format);

    Set<String> getEntries(String section, String locale);

    Component getMessageComponent(String locale, String moduleId, String messageId, Object[] format);

    class Impl implements Language {
        public static final String TEMPLATE_DIR = "/templates/lang/%s.%s.yml";

        private final HashMap<String, YamlConfiguration> sections = new HashMap<>();
        private final String id;
        private final String ownerId;
        private final Plugin owner;

        public Impl(Plugin owner, String id) {
            this.owner = owner;
            this.ownerId = "quark";
            ConfigDelegation.LANGUAGE_REGISTRY.put(id, this);
            this.id = id;
            this.reload();
            this.sync(false);
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

        @Override
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

        @Override
        public void restore() {
            for (String id : ENABLED_LANGUAGES) {
                String fileDir = "%s/lang/%s/%s.yml".formatted(FilePath.pluginFolder(this.ownerId), BukkitUtil.fixLocaleId(id), this.id);
                String srcDir = TEMPLATE_DIR.formatted(this.id, BukkitUtil.fixLocaleId(id));
                FilePath.coverFile(srcDir, fileDir);
                this.load(id, srcDir, fileDir);
            }
        }

        @Override
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

        @Override
        public String buildUI(ConfigurationSection parentSection, String id, String module, String locale, Function<String, String> sourceProcessor) {
            String ui = sourceProcessor.apply(generateMessage(parentSection, id));
            return this.processMessageWithLocalVars(ui, module, locale);
        }

        @Override
        public String buildUI(String source, String module, String locale) {
            return this.processMessageWithLocalVars(source, module, locale);
        }


        //trigger
        @Override
        public String getMessage(String locale, String moduleId, String messageId, Object... format) {
            String msgRaw = getRawMessage(locale, moduleId, messageId);
            if (msgRaw.startsWith("ERROR:")) {
                return msgRaw;
            }
            try {
                return processMessageWithLocalVars(msgRaw.formatted(format), moduleId, locale);
            } catch (Exception e) {
                return "ERROR: FORMAT_FAILED(%s)".formatted(msgRaw);
            }
        }

        @Override
        public String getRandomMessage(String locale, String moduleId, String msg, Object... format) {
            return getRandomRawMessage(locale, moduleId, msg).formatted(format);
        }

        @Override
        public List<String> getMessageList(String locale, String moduleId, String messageId) {
            return getMessageListFromConfig(locale, moduleId, messageId);
        }

        @Override
        public void sendMessageTo(CommandSender target, String moduleId, String msg, Object... format) {
            String locale = target instanceof Player ? PlayerUtil.getLocale(((Player) target)) : "en_us";
            TextSender.sendLine(target, TextBuilder.buildComponent(getMessage(locale, moduleId, msg, format)));
        }

        @Override
        public void broadcastMessage(boolean opOnly, String moduleId, String msg, Object... format) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!opOnly) {
                    sendMessageTo(p, moduleId, msg, format);
                    continue;
                }
                if (p.isOp()) {
                    sendMessageTo(p, moduleId, msg, format);
                }
            }
        }

        @Override
        public LanguageEntry createEntry(String moduleId) {
            return new LanguageEntry(this, moduleId);
        }


        //message
        private String getRawMessage(String locale, String namespace, String id) {
            String msgRaw = this.getMessageFromConfig(locale, namespace, id);
            if (msgRaw.startsWith("ERROR:")) {
                return msgRaw;
            }
            return this.processMessageWithLocalVars(msgRaw, namespace, locale);
        }

        private String getRandomRawMessage(String locale, String namespace, String id) {
            List<String> list = this.getMessageList(locale, namespace, id);
            return this.processMessageWithLocalVars(list.get(new Random().nextInt(0, list.size())), namespace, locale);
        }


        //config access
        private String getMessageFromConfig(String locale, String namespace, String id) {
            id = Identifiers.external(id);
            namespace = Identifiers.external(namespace);

            ConfigurationSection section = this.getNameSpace(locale, namespace);
            if (section == null) {
                section = this.getNameSpace("zh_cn", namespace);
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
        private List<String> getMessageListFromConfig(String locale, String namespace, String id) {
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

        private ConfigurationSection getNameSpace(String locale, String namespace) {
            namespace = Identifiers.external(namespace);
            ConfigurationSection root = this.getRootSection(locale);
            if (root == null) {
                root = this.getRootSection(CHINESE_SIMPLIFIED);
            }
            if (root == null) {
                return null;
            }
            return root.getConfigurationSection(namespace);
        }


        private ConfigurationSection getRootSection(String locale) {
            locale = BukkitUtil.fixLocaleId(locale);
            ConfigurationSection section = this.sections.get(locale);
            if (section == null) {
                return null;
            }
            return section.getConfigurationSection("language");
        }


        //process
        private String processMessageWithLocalVars(String src, String namespace, String locale) {
            namespace = Identifiers.external(namespace);
            Matcher matcher = MESSAGE_PATTERN.matcher(src);
            while (matcher.find()) {
                String s = matcher.group();
                src = src.replace(s, this.getRawMessage(locale, namespace, s.substring(5, s.length() - 1)));
            }
            Matcher matcher2 = RANDOM_MESSAGE_PATTERN.matcher(src);
            while (matcher2.find()) {
                String s = matcher2.group();
                src = src.replace(s, this.getRandomRawMessage(locale, namespace, s.substring(6, s.length() - 1)));
            }
            Matcher matcher3 = LOCALIZED_GLOBAL_VAR.matcher(src);
            while (matcher3.find()) {
                String s = matcher3.group();
                src = src.replace(s, Quark.LANGUAGE.getMessage(locale, "global-vars", s.substring(8, s.length() - 1)));
            }
            return Queries.GLOBAL_TEMPLATE_ENGINE.handle(src);
        }

        @Override
        public Set<String> getEntries(String section, String locale) {
            return this.getRootSection(locale).getConfigurationSection(section).getKeys(false);
        }

        @Override
        public Component getMessageComponent(String locale, String moduleId, String messageId, Object... format) {
            return TextBuilder.buildComponent(this.getMessage(locale, moduleId, messageId, format));
        }
    }
}
