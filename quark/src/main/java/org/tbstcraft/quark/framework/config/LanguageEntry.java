package org.tbstcraft.quark.framework.config;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.framework.text.TextBuilder;
import org.tbstcraft.quark.framework.text.TextSender;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class LanguageEntry {
    private final String moduleId;
    private final Language languageFile;

    public LanguageEntry(Language languageFile, String moduleId) {
        this.moduleId = moduleId;
        this.languageFile = languageFile;
    }

    public String getMessage(String locale, String messageId, Object... format) {
        return this.languageFile.getMessage(locale, this.moduleId, messageId, format);
    }

    public Component getMessageComponent(String locale, String messageId, Object... format) {
        return this.languageFile.getMessageComponent(locale, this.moduleId, messageId, format);
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

    public String buildUI(ConfigurationSection source, String id, String locale, Function<String, String> sourceProcessor) {
        return this.languageFile.buildUI(source, id, this.moduleId, locale, sourceProcessor);
    }

    public String buildUI(ConfigurationSection source, String id, String locale) {
        return this.languageFile.buildUI(source, id, this.moduleId, locale, (msg) -> msg);
    }

    public String buildUI(String source, String locale) {
        return this.languageFile.buildUI(source, this.moduleId, locale);
    }

    public void sendUI(CommandSender sender, ConfigurationSection uiContainer, String id, Function<String, String> sourceProcessor) {
        String src = this.buildUI(uiContainer, id, Language.getLocale(sender), sourceProcessor);
        TextSender.sendBlock(sender, TextBuilder.build(src));
    }

    public Set<String> getEntries(String section, String locale) {
        return this.languageFile.getEntries(section,locale);
    }
}
