package org.tbstcraft.quark.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.tbstcraft.quark.config.Queries;

import java.util.Set;

public final class ComponentBlockBuilder {
    private final StringBuilder buffer = new StringBuilder(32);
    private TextComponent.Builder builder = Component.text();
    private Component[] format = new Component[0];
    private String tag;

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
            case '{' -> this.endNode();
            case '}' -> {
                this.tag = buffer.toString();
                if (this.tag.matches("-?\\d+(\\.\\d+)?")) {
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
