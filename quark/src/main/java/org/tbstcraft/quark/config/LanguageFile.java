package org.tbstcraft.quark.config;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.util.BukkitUtil;
import org.tbstcraft.quark.util.FilePath;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public final class LanguageFile {
    public static final String CHINESE_SIMPLIFIED = "zh_cn";
    public static final String ENGLISH = "en";
    public static final String FRENCH = "fr";
    public static final String JAPANESE = "jp";
    public static final String RUSSIA = "ru";

    public static final Logger LOGGER = Quark.PLUGIN.getLogger();
    public static final HashMap<String, LanguageFile> REGISTERED_CACHE = new HashMap<>();

    private final HashMap<String, ConfigurationSection> sections = new HashMap<>();
    private final String packageId;

    public LanguageFile(String packageId) {
        REGISTERED_CACHE.put(packageId, this);
        this.packageId = packageId;
        this.reload();
    }

    public static void reloadAll() {
        for (LanguageFile languageFile : REGISTERED_CACHE.values()) {
            languageFile.reload();
        }
    }

    public static void restoreAll() {
        for (LanguageFile languageFile : REGISTERED_CACHE.values()) {
            languageFile.restore();
        }
    }

    public static String formatGlobal(String msg) {
        for (Object key : SharedObjects.GLOBAL_VARS.keySet()) {
            msg = msg.replace("{" + key + "}", SharedObjects.GLOBAL_VARS.getProperty(key.toString()));
        }
        return msg;
    }

    public void restore() {
        this.restore(CHINESE_SIMPLIFIED);
        this.restore(ENGLISH);
        this.restore(FRENCH);
        this.restore(JAPANESE);
        this.restore(RUSSIA);
    }

    public void reload() {
        this.load(CHINESE_SIMPLIFIED);
        this.load(ENGLISH);
        this.load(FRENCH);
        this.load(JAPANESE);
        this.load(RUSSIA);
    }

    private void restore(String locale) {
        FilePath.coverLanguageFile(this.packageId, locale);
        this.load(locale);
    }

    private void load(String locale) {
        File file = FilePath.languageFile(this.packageId, locale);
        if (!file.exists()) {
            return;
        }
        try {
            ConfigurationSection section = YamlConfiguration.loadConfiguration(file);
            this.sections.put(BukkitUtil.fixLocaleId(locale), section);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ConfigurationSection getNameSpace(String locale, String moduleId) {
        ConfigurationSection root = this.getRootSection(locale);
        if (root == null) {
            root = this.getRootSection(CHINESE_SIMPLIFIED);
        }
        if (root == null) {
            return null;
        }
        return root.getConfigurationSection(moduleId);
    }

    private String getMessageFromConfig(String locale, String moduleId, String messageId) {
        ConfigurationSection section = this.getNameSpace(locale, moduleId);
        if (section == null) {
            return "ERROR: SECTION_NOT_FOUND(%s/%s)".formatted(locale, moduleId);
        }
        if (!section.contains(messageId)) {
            return "ERROR: MESSAGE_NOT_FOUND(%s/%s)".formatted(locale, messageId);
        }
        if (section.isString(messageId)) {
            return section.getString(messageId);
        }
        List<String> list = section.getStringList(messageId);
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

    private List<String> getMessageListFromConfig(String locale, String moduleId, String messageId) {
        ConfigurationSection section = this.getNameSpace(locale, moduleId);
        if (section == null) {
            return List.of("ERROR: SECTION_NOT_FOUND(%s)".formatted(moduleId));
        }
        if (!section.contains(messageId)) {
            return List.of("ERROR: MESSAGE_NOT_FOUND(%s)".formatted(messageId));
        }
        return section.getStringList(messageId);
    }

    private ConfigurationSection getRootSection(String locale) {
        locale = BukkitUtil.fixLocaleId(locale);
        ConfigurationSection section= this.sections.get(locale);
        if(section==null){
            return null;
        }
        return section.getConfigurationSection("language");
    }

    public String getMessage(String locale, String moduleId, String messageId, Object... format) {
        String msgRaw = this.getMessageFromConfig(locale, moduleId, messageId);
        if (msgRaw.startsWith("ERROR:")) {
            return msgRaw;
        }
        try {
            return formatGlobal(BukkitUtil.formatChatComponent(msgRaw.formatted(format)));
        } catch (Exception e) {
            return "ERROR: FORMAT_FAILED(%s)".formatted(msgRaw);
        }
    }

    public List<String> getMessageList(String locale, String moduleId, String messageId) {
        return getMessageListFromConfig(locale, moduleId, messageId);
    }

    public void sendMessageTo(CommandSender target, String moduleId, String msg, Object... format) {
        String locale = target instanceof Player ? ((Player) target).getLocale() : "en_us";
        target.sendMessage(this.getMessage(locale, moduleId, msg, format));
    }

    public void broadcastMessage(boolean opOnly, String moduleId, String msg, Object... format) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!opOnly) {
                p.sendMessage(this.getMessage(p.getLocale(), moduleId, msg, format));
                continue;
            }
            if (p.isOp()) {
                p.sendMessage(this.getMessage(p.getLocale(), moduleId, msg, format));
            }
        }
    }

    public LanguageEntry getEntry(String moduleId) {
        return new LanguageEntry(this, moduleId);
    }

    public String getRandomMessage(String locale, String moduleId, String msg, Object... format) {
        List<String> list = this.getMessageList(locale, moduleId, msg);

        return formatGlobal(BukkitUtil.formatChatComponent(list.get(new Random().nextInt(0, list.size())).formatted(format)));
    }
}
