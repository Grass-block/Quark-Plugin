package org.tbstcraft.quark.data.language;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.text.ComponentBlock;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LanguageItem {
    private final ILanguageAccess parent;
    private final String namespace;
    private final String id;

    public LanguageItem(ILanguageAccess parent, String namespace, String id) {
        this.parent = parent;
        this.namespace = namespace;
        this.id = id;
    }

    public static LanguageItem dummy(String msg) {
        return new Dummy(msg);
    }

    //raw
    public String getRawMessage(Locale locale) {
        return this.parent.getRawMessage(locale, this.namespace, this.id);
    }

    public List<String> getRawMessageList(Locale locale) {
        return this.parent.getRawMessageList(locale, this.namespace, this.id);
    }

    public String getRawRandomMessage(Locale locale) {
        return this.parent.getRawRandomMessage(locale, this.namespace, this.id);
    }

    //inline
    public String getInlineMessage(Locale locale) {
        return this.parent.getInlineMessage(locale, this.namespace, this.id);
    }

    public String getInlineRandomMessage(Locale locale) {
        return this.parent.getInlineRandomMessage(locale, this.namespace, this.id);
    }

    public List<String> getInlineMessageList(Locale locale) {
        return this.parent.getInlineMessageList(locale, this.namespace, this.id);
    }

    //completed
    public String getMessage(Locale locale, Object... format) {
        return this.parent.getMessage(locale, this.namespace, this.id, format);
    }

    public String getRandomMessage(Locale locale, Object... format) {
        return this.parent.getRandomMessage(locale, this.namespace, this.id, format);
    }

    public List<String> getMessageList(Locale locale) {
        return this.parent.getMessageList(locale, this.namespace, this.id);
    }

    //component
    public ComponentBlock getMessageComponent(Locale locale, Object... format) {
        return this.parent.getMessageComponent(locale, this.namespace, this.id, format);
    }

    public ComponentBlock getRandomMessageComponent(Locale locale, Object... format) {
        return this.parent.getRandomMessageComponent(locale, this.namespace, this.id, format);
    }

    //send
    public void sendMessage(CommandSender sender, Object... format) {
        this.parent.sendMessage(sender, this.namespace, this.id, format);
    }

    public void sendRandomMessage(CommandSender sender, Object... format) {
        this.parent.sendRandomMessage(sender, this.namespace, this.id, format);
    }

    public void broadcastMessage(boolean op, boolean console, Object... format) {
        this.parent.broadcastMessage(op, console, this.namespace, this.id, format);
    }

    public void broadcastRandomMessage(boolean op, boolean console, Object... format) {
        this.parent.broadcastRandomMessage(op, console, this.namespace, this.id, format);
    }

    public Set<Locale> getIncludeLocales() {
        return Set.of();
    }

    public String getId() {
        return "%s:%s".formatted(this.namespace, this.id);
    }

    public ILanguageAccess getParent() {
        return this.parent;
    }

    private static final class Dummy extends LanguageItem {
        private final String msg;

        public Dummy(String msg) {
            super(null, null, null);
            this.msg = msg;
        }

        @Override
        public String getRawMessage(Locale locale) {
            return this.msg;
        }

        @Override
        public List<String> getRawMessageList(Locale locale) {
            return List.of(this.msg);
        }

        @Override
        public String getRawRandomMessage(Locale locale) {
            return this.msg;
        }

        @Override
        public String getInlineMessage(Locale locale) {
            return this.msg;
        }

        @Override
        public String getInlineRandomMessage(Locale locale) {
            return this.msg;
        }

        @Override
        public List<String> getInlineMessageList(Locale locale) {
            return List.of(this.msg);
        }

        @Override
        public String getMessage(Locale locale, Object... format) {
            return this.msg;
        }

        @Override
        public String getRandomMessage(Locale locale, Object... format) {
            return this.msg;
        }

        @Override
        public List<String> getMessageList(Locale locale) {
            return List.of(this.msg);
        }

        @Override
        public ComponentBlock getMessageComponent(Locale locale, Object... format) {
            var b = new ComponentBlock();
            b.add(Component.text(getMessage(locale, format)));
            return b;
        }

        @Override
        public ComponentBlock getRandomMessageComponent(Locale locale, Object... format) {
            return getMessageComponent(locale, format);
        }
    }
}
