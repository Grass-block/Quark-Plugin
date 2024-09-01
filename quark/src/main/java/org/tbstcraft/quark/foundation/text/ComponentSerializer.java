package org.tbstcraft.quark.foundation.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;

@SuppressWarnings("deprecation")
public interface ComponentSerializer {
    static String legacy(ComponentLike component) {
        return LegacyComponentSerializer.legacySection().serialize(component.asComponent());
    }

    static String json(ComponentLike component){
        return GsonComponentSerializer.gson().serialize(component.asComponent());
    }

    static BaseComponent[] bungee(ComponentLike component){
        return net.md_5.bungee.chat.ComponentSerializer.parse(json(component));
    }

    static Component json(String json) {
        return GsonComponentSerializer.gson().deserialize(json);
    }
}
