package org.tbstcraft.quark.framework.data.language;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.framework.data.config.Language;
import org.tbstcraft.quark.util.text.ComponentBlock;
import org.tbstcraft.quark.util.text.TextBuilder;
import org.tbstcraft.quark.util.text.TextSender;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;

public final class LanguageAccess {
    private static final Random RANDOM = new Random();
    private final LanguageContainer parent;

    public LanguageAccess(LanguageContainer parent) {
        this.parent = parent;
    }

    private String inlineMessage(String src, Locale locale, String namespace) {
        Matcher matcher = Language.MESSAGE_PATTERN.matcher(src);
        while (matcher.find()) {
            String s = matcher.group();

            String[] id = s.substring(5, s.length() - 1).split(":");
            String ns = id.length == 1 ? namespace : id[0];
            src = src.replace(s, getInlineMessage(locale, ns, id[id.length - 1]));
        }
        Matcher matcher2 = Language.RANDOM_MESSAGE_PATTERN.matcher(src);
        while (matcher2.find()) {
            String s = matcher.group();

            String[] id = s.substring(6, s.length() - 1).split(":");
            String ns = id.length == 1 ? namespace : id[0];
            src = src.replace(s, getInlineRandomMessage(locale, ns, id[id.length - 1]));
        }
        Matcher matcher3 = Language.LOCALIZED_GLOBAL_VAR.matcher(src);
        while (matcher3.find()) {
            String s = matcher3.group();
            //src = src.replace(s, Quark.LANGUAGE.getInlineMessage(locale, "global-vars", s.substring(8, s.length() - 1)));
        }
        return src;
    }


    //raw
    public String getRawMessage(Locale locale, String namespace, String id) {
        return this.parent.getMessage(locale, namespace, id);
    }

    public List<String> getRawMessageList(Locale locale, String namespace, String id) {
        return this.parent.getMessageList(locale, namespace, id);
    }

    public String getRawRandomMessage(Locale locale, String namespace, String id) {
        List<String> list = getRawMessageList(locale, namespace, id);
        return list.get(RANDOM.nextInt(list.size()));
    }

    //inline
    public String getInlineMessage(Locale locale, String namespace, String id) {
        String msg = getRawMessage(locale, namespace, id);
        return inlineMessage(msg, locale, namespace);
    }

    public String getInlineRandomMessage(Locale locale, String namespace, String id) {
        String msg = getRawRandomMessage(locale, namespace, id);
        return inlineMessage(msg, locale, namespace);
    }

    public List<String> getInlineMessageList(Locale locale, String namespace, String id) {
        List<String> msg = getRawMessageList(locale, namespace, id);

        msg.replaceAll(src -> inlineMessage(src, locale, namespace));
        return msg;
    }


    //completed
    public String getMessage(Locale locale, String namespace, String id, Object... format) {
        return Language.format(getInlineMessage(locale, namespace, id));
    }

    public String getRandomMessage(Locale locale, String namespace, String id, Object... format) {
        return Language.format(getInlineRandomMessage(locale, namespace, id));
    }

    public List<String> getMessageList(Locale locale, String namespace, String id, Object... format) {
        List<String> msg = getInlineMessageList(locale, namespace, id);

        msg.replaceAll(Language::format);
        return msg;
    }

    //component
    public ComponentBlock getMessageComponent(Locale locale, String namespace, String id, Object... format) {
        return TextBuilder.build(getMessage(locale, namespace, id, format));
    }

    public ComponentBlock getRandomMessageComponent(Locale locale, String namespace, String id, Object... format) {
        return TextBuilder.build(getRandomMessage(locale, namespace, id, format));
    }

    //send
    public void sendMessage(CommandSender sender, String namespace, String id, Object... format) {
        TextSender.sendTo(sender, getMessageComponent(Language.locale(sender), namespace, id, format));
    }

    public void sendRandomMessage(CommandSender sender, String namespace, String id, Object... format) {
        TextSender.sendTo(sender, getRandomMessageComponent(Language.locale(sender), namespace, id, format));
    }

    public void broadcastMessage(boolean op, boolean console, String namespace, String id, Object... format) {
        TextSender.broadcastBlock((l) -> getMessageComponent(l, namespace, id, format), op, console);
    }

    public void broadcastRandomMessage(boolean op, boolean console, String namespace, String id, Object... format) {
        TextSender.broadcastBlock((l) -> getRandomMessageComponent(l, namespace, id, format), op, console);
    }
}
