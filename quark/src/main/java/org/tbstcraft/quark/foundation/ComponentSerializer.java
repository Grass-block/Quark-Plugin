package org.tbstcraft.quark.foundation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;

@SuppressWarnings("deprecation")
public interface ComponentSerializer {

    static String json(ComponentLike component) {
        return GsonComponentSerializer.gson().serialize(component.asComponent());
    }

    static Component json(String json) {
        return GsonComponentSerializer.gson().deserialize(json);
    }

    static BaseComponent[] bungee(ComponentLike component) {
        return net.md_5.bungee.chat.ComponentSerializer.parse(json(component));
    }

    static String legacy(ComponentLike component) {
        return LegacyComponentSerializer.legacySection().serialize(component.asComponent());
    }

    static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component).replaceAll("§([0-9]|[a-f]|m|n|o|k)","");
    }
}
