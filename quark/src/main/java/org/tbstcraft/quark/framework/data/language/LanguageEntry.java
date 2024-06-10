package org.tbstcraft.quark.framework.data.language;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.util.text.ComponentBlock;

import java.util.List;
import java.util.Locale;

public final class LanguageEntry{
    private final ILanguageAccess parent;
    private final String namespace;

    public LanguageEntry(ILanguageAccess parent, String namespace) {
        this.parent = parent;
        this.namespace = namespace;
    }

    //raw
    public String getRawMessage(Locale locale, String id) {
        return this.parent.getRawMessage(locale, this.namespace, id);
    }

    public List<String> getRawMessageList(Locale locale, String id) {
        return this.parent.getRawMessageList(locale, this.namespace, id);
    }

    public String getRawRandomMessage(Locale locale, String id) {
        return this.parent.getRawRandomMessage(locale, this.namespace, id);
    }

    //inline
    public String getInlineMessage(Locale locale, String id) {
        return this.parent.getInlineMessage(locale, this.namespace, id);
    }

    public String getInlineRandomMessage(Locale locale, String id) {
        return this.parent.getInlineRandomMessage(locale, this.namespace, id);
    }

    public List<String> getInlineMessageList(Locale locale, String id) {
        return this.parent.getInlineMessageList(locale, this.namespace, id);
    }


    //completed
    public String getMessage(Locale locale, String id, Object... format) {
        return this.parent.getMessage(locale, this.namespace, id, format);
    }

    public String getRandomMessage(Locale locale, String id, Object... format) {
        return this.parent.getRandomMessage(locale, this.namespace, id, format);
    }

    public List<String> getMessageList(Locale locale, String id) {
        return this.parent.getMessageList(locale, this.namespace, id);
    }


    //component
    public ComponentBlock getMessageComponent(Locale locale, String id, Object... format) {
        return this.parent.getMessageComponent(locale, this.namespace, id, format);
    }

    public ComponentBlock getRandomMessageComponent(Locale locale, String id, Object... format) {
        return this.parent.getRandomMessageComponent(locale, this.namespace, id, format);
    }

    //send
    public void sendMessage(CommandSender sender, String id, Object... format) {
        this.parent.sendMessage(sender, this.namespace, id, format);
    }

    public void sendRandomMessage(CommandSender sender, String id, Object... format) {
        this.parent.sendRandomMessage(sender, this.namespace, id, format);
    }

    public void broadcastMessage(boolean op, boolean console, String id, Object... format) {
        this.parent.broadcastMessage(op, console, this.namespace, id, format);
    }

    public void broadcastRandomMessage(boolean op, boolean console, String id, Object... format) {
        this.parent.broadcastRandomMessage(op, console, this.namespace, id, format);
    }

    //template
    public String buildTemplate(Locale locale, String template) {
        return this.parent.buildTemplate(locale, this.namespace, template);
    }

    public void sendTemplate(CommandSender sender, String template) {
        this.parent.sendTemplate(sender, this.namespace, template);
    }
}
