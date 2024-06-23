package org.tbstcraft.quark.foundation.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ComponentBlock extends ArrayList<Component> implements ComponentLike {
    public Component toSingleLine() {
        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < this.size() - 1; i++) {
            builder.append(this.get(i));
            builder.append(Component.text("\n"));
        }
        builder.append(this.get(this.size() - 1));
        return builder.build();
    }

    public void send(CommandSender sender) {
        for (Component line : this) {
            TextSender.sendLine(sender, line);
        }
    }

    public ComponentBlock append(ComponentBlock block) {
        this.addAll(block);
        return this;
    }

    public ComponentBlock appendBefore(ComponentBlock block) {
        List<Component> list = new ArrayList<>();
        list.addAll(block);
        list.addAll(this);
        this.clear();
        this.addAll(list);
        return this;
    }

    @Override
    public @NotNull Component asComponent() {
        return toSingleLine();
    }

    @Override
    public String toString() {
        return LegacyComponentSerializer.legacySection().serialize(this.toSingleLine());
    }

    public String toPlainTextString() {
        return PlainTextComponentSerializer.plainText().serialize(asComponent());
    }
}
