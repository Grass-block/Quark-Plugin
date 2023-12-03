package org.tbstcraft.quark.config;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.util.BukkitUtil;

import java.util.List;
import java.util.Locale;

public final class LanguageEntry {
    private final String moduleId;
    private final LanguageFile languageFile;

    public LanguageEntry(LanguageFile languageFile, String moduleId) {
        this.moduleId = moduleId;
        this.languageFile = languageFile;
    }

    public String getMessage(String locale, String messageId, Object... format) {
        return this.languageFile.getMessage(locale, this.moduleId, messageId, format);
    }

    public List<String> getMessageList(String locale, String messageId) {
        return this.languageFile.getMessageList(locale, this.moduleId, messageId);
    }

    public void sendMessageTo(CommandSender target, String msg, Object... format) {
        this.languageFile.sendMessageTo(target, this.moduleId, msg, format);
    }

    public void broadcastMessage(boolean opOnly, String msg, Object... format) {
        this.languageFile.broadcastMessage(opOnly, this.moduleId, msg, format);
    }

    public String getRandomMessage(String locale, String msg, Object... format) {
        return this.languageFile.getRandomMessage(locale, this.moduleId, msg, format);
    }
}
