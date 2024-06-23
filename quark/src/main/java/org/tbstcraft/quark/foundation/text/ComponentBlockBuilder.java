package org.tbstcraft.quark.foundation.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.tbstcraft.quark.data.config.Queries;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ComponentBlockBuilder {
    public static final Pattern TAG_FILTER = Pattern.compile("\\{(color|click|underline|magic|reset|hover|none|[0-9]|;)[^}]*}");
    public static final Pattern URL_FILTER = Pattern.compile("(https?://)?(([0-9a-z.]+\\.[a-z]+)|(([0-9]{1,3}\\.){3}[0-9]{1,3}))(:[0-9]+)?(/[0-9a-z%/.\\-_]*)?(\\?[0-9a-z=&%_\\-]*)?(#[0-9a-z=&%_\\-]*)?");

    private final StringBuilder buffer = new StringBuilder(32);
    private TextComponent.Builder builder = Component.text();
    private Component[] format = new Component[0];
    private String tag;

    public static String preprocessTags(String s) {
        Matcher m = TAG_FILTER.matcher(s);
        while (m.find()) {
            String s2 = m.group();
            s = s.replace(s2, m.group().replace("{", "\ufff1").replace("}", "\ufff7"));
        }

        return s;
    }

    public static String processURLs(String input) {
        String replacementText = "{click(link,%s)}%s{;}";
        Matcher matcher = URL_FILTER.matcher(input);

        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String url = matcher.group();
            if (!isInsideBraces(input, matcher.start())) {
                matcher.appendReplacement(result, replacementText.formatted(url, url));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(url));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static boolean isInsideBraces(String input, int index) {
        for (int i = index; i >= 0; i--) {
            if (input.charAt(i) == '{') {
                return true;
            }
            if (input.charAt(i) == '}') {
                return false;
            }
        }
        return false;
    }

    private void endNode() {
        String content = this.buffer.toString();
        this.buffer.delete(0, this.buffer.length());
        if (this.tag == null) {
            this.builder.append(Component.text(content));
            return;
        }
        Set<String> args = Set.of(this.tag.split(";"));
        if (args.contains("return")) {
            this.builder.append(Component.newline());
            return;
        }
        this.builder.append(ComponentGenerator.buildComponent(content, args));
    }

    public ComponentBlock build(String raw, Component... format) {
        raw = Queries.GLOBAL_TEMPLATE_ENGINE.handle(raw);

        raw = processURLs(raw);
        raw = preprocessTags(raw);

        this.format = format;
        this.builder = Component.text();
        this.clearBuffer();
        ComponentBlock block = new ComponentBlock();

        for (int i = 0; i < raw.length(); i++) {
            this.dispatchChar(raw.charAt(i), block);
        }
        this.endNode();
        block.add(this.builder.build());

        return block;
    }

    private void dispatchChar(char code, ComponentBlock block) {
        switch (code) {
            case '\ufff1' -> this.endNode();
            case '\ufff7' -> {
                this.tag = buffer.toString();
                if (this.tag.matches("[0-9]")) {
                    this.builder.append(this.format[Integer.parseInt(this.tag)]);
                }
                this.clearBuffer();
            }
            case '\n' -> {
                this.endNode();
                block.add(this.builder.build());
                this.builder = Component.text();
            }
            default -> this.buffer.append(code);
        }
    }

    private void clearBuffer() {
        this.buffer.delete(0, this.buffer.length());
    }
}
