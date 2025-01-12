package org.atcraftmc.quark;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.atcraftmc.qlib.texts.TextBuilder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public final class CustomChatRenderer implements ChatRenderer {
    private final List<Component> prefixes = new ArrayList<>();
    private final List<Component> postFixes = new ArrayList<>();
    private String template = "<{0}> {1}";

    public static CustomChatRenderer renderer(AsyncChatEvent event) {
        if (event.renderer() instanceof CustomChatRenderer renderer) {
            return renderer;
        }
        CustomChatRenderer renderer = new CustomChatRenderer();
        event.renderer(renderer);
        return renderer;
    }

    public CustomChatRenderer prefixNearest(ComponentLike comp) {
        this.prefixes.add(comp.asComponent());
        return this;
    }

    public CustomChatRenderer prefix(ComponentLike comp) {
        this.prefixes.add(0, comp.asComponent());
        return this;
    }

    public CustomChatRenderer postfix(ComponentLike comp) {
        this.postFixes.add(comp.asComponent());
        return this;
    }

    public CustomChatRenderer postfixNearest(ComponentLike comp) {
        this.postFixes.add(0, comp.asComponent());
        return this;
    }

    public CustomChatRenderer template(String template) {
        this.template = template;
        return this;
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        TextComponent.Builder builder = Component.text();

        for (Component c : this.prefixes) {
            builder.append(c);
        }

        builder.append(TextBuilder.buildComponent(this.template, sourceDisplayName, message));

        for (Component c : this.postFixes) {
            builder.append(c);
        }

        return builder.build();
    }
}
