package org.tbstcraft.quark.text;

import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashSet;
import java.util.Set;

public interface ComponentGenerator {
    MiniMessage MINI_MESSAGE_DISPATCHER = MiniMessage.builder().build();

    static String[] getFunction(String name, Set<String> args) {
        for (String s : args) {
            if (!s.startsWith(name)) {
                continue;
            }
            String arguments = s.substring(name.length() + 1, s.length() - 1);
            return arguments.split(",");
        }
        return null;
    }

    static Set<TextDecoration> decorations(Set<String> args) {
        Set<TextDecoration> decorations = new HashSet<>();
        if (args.contains("italic")) {
            decorations.add(TextDecoration.ITALIC);
        }
        if (args.contains("bold")) {
            decorations.add(TextDecoration.BOLD);
        }
        if (args.contains("delete")) {
            decorations.add(TextDecoration.STRIKETHROUGH);
        }
        if (args.contains("obfuscate")) {
            decorations.add(TextDecoration.OBFUSCATED);
        }
        if (args.contains("underline")) {
            decorations.add(TextDecoration.UNDERLINED);
        }
        if (args.contains("-underline")) {
            decorations.add(TextDecoration.UNDERLINED);
        }
        return decorations;
    }

    static ComponentBuilder<?, ?> color(ComponentBuilder<?, ?> builder, Set<String> args) {
        String[] col = getFunction("color", args);
        String content = content(builder);
        if (col == null) {
            return builder;
        }

        //gradient parsing
        if (col.length > 1 && !content.contains(">")) {
            StringBuilder sb = new StringBuilder("<gradient:");
            for (String c : col) {
                if (!c.startsWith("#")) {
                    c = matchTextColor(c).asHexString();
                }
                sb.append(c).append(":");
            }
            sb.delete(sb.length() - 1, sb.length());
            sb.append(">").append(content).append("</gradient>");
            return ((TextComponent) MINI_MESSAGE_DISPATCHER.deserialize(sb.toString())).toBuilder();
        }

        String colorArg = col[0];

        if (colorArg.startsWith("#")) {
            builder.color(TextColor.color(Integer.parseInt(colorArg.replace("#", ""), 16)));
        } else {
            builder.color(matchTextColor(colorArg));
        }
        return builder;
    }

    static TextColor matchTextColor(String id) {
        return switch (id) {
            case "black" -> NamedTextColor.BLACK;
            case "dark_blue" -> NamedTextColor.DARK_BLUE;
            case "dark_green" -> NamedTextColor.DARK_GREEN;
            case "dark_aqua" -> NamedTextColor.DARK_AQUA;
            case "dark_red" -> NamedTextColor.DARK_RED;
            case "dark_purple" -> NamedTextColor.DARK_PURPLE;
            case "gold" -> NamedTextColor.GOLD;
            case "gray" -> NamedTextColor.GRAY;
            case "dark_gray" -> NamedTextColor.DARK_GRAY;
            case "blue" -> NamedTextColor.BLUE;
            case "green" -> NamedTextColor.GREEN;
            case "aqua" -> NamedTextColor.AQUA;
            case "red" -> NamedTextColor.RED;
            case "purple" -> NamedTextColor.LIGHT_PURPLE;
            case "yellow" -> NamedTextColor.YELLOW;
            default -> NamedTextColor.WHITE;
        };
    }

    static Style style(Set<String> args) {
        Style style = Style.style(decorations(args));

        if (args.contains("-italic")) {
            style.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        }
        if (args.contains("-bold")) {
            style.decoration(TextDecoration.BOLD, TextDecoration.State.FALSE);
        }
        if (args.contains("-delete")) {
            style.decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE);
        }
        if (args.contains("-obfuscate")) {
            style.decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE);
        }
        if (args.contains("-underline")) {
            style.decoration(TextDecoration.UNDERLINED, TextDecoration.State.FALSE);
        }
        if (args.contains("none")) {
            style.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
            style.decoration(TextDecoration.BOLD, TextDecoration.State.FALSE);
            style.decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE);
            style.decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE);
            style.decoration(TextDecoration.UNDERLINED, TextDecoration.State.FALSE);
        }
        return style;
    }

    static ClickEvent clickEvent(Set<String> args) {
        String[] arguments = getFunction("click", args);

        if (arguments == null) {
            return null;
        }

        String action = arguments[0];
        String value = arguments[1];

        return switch (action) {
            case "link" -> ClickEvent.openUrl(value);
            case "command" -> ClickEvent.runCommand(value);
            case "copy" -> ClickEvent.copyToClipboard(value);
            case "suggest_cmd" -> ClickEvent.suggestCommand(value);
            default -> throw new IllegalStateException("Unexpected value: " + arguments[0]);
        };
    }

    // TODO: 2024/3/9 Add more hover options
    static HoverEvent<?> hoverEvent(Set<String> args) {
        String[] arguments = getFunction("hover", args);

        if (arguments == null) {
            return null;
        }

        String action = arguments[0];
        String value = arguments[1];
        return switch (action) {
            case "text" -> HoverEvent.showText(Component.text(value));
            case "item" -> null;
            default -> throw new IllegalStateException("Unexpected value: " + action);
        };
    }

    static Component buildComponent(String content, Set<String> tag) {
        ComponentBuilder<?, ?> append;
        if (tag.contains("minimsg")) {
            append = ((TextComponent) MINI_MESSAGE_DISPATCHER.deserialize(content)).toBuilder();
        } else if (tag.contains("keybind")) {
            append = Component.keybind(content).toBuilder();
        } else if (tag.contains("translatable")) {
            append = Component.translatable(content).toBuilder();
        } else if (tag.contains("selector")) {
            append = Component.selector(content).toBuilder();
        } else {
            append = Component.text(content).toBuilder();
        }

        append.style(style(tag));
        append = color(append, tag);

        append.clickEvent(clickEvent(tag));
        append.hoverEvent(hoverEvent(tag));
        return append.build();
    }

    static String content(ComponentBuilder<?, ?> builder) {
        if (builder instanceof TextComponent.Builder b) {
            return b.content();
        }
        if (builder instanceof TranslatableComponent.Builder b) {
            return b.build().key();
        }
        if (builder instanceof KeybindComponent.Builder b) {
            return b.build().keybind();
        }
        return "";
    }
}
