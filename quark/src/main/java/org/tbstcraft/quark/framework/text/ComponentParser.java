package org.tbstcraft.quark.framework.text;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

@SuppressWarnings("deprecation")
public interface ComponentParser {
    static String asString(ComponentLike component) {
        return LegacyComponentSerializer.legacySection().serialize(component.asComponent());
    }

    static String asJson(ComponentLike component){
        return GsonComponentSerializer.gson().serialize(component.asComponent());
    }

    static BaseComponent[] asBungee(ComponentLike component){
        return ComponentSerializer.parse(asJson(component));
    }
}
