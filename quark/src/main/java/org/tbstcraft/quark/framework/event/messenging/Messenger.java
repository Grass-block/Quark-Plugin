package org.tbstcraft.quark.framework.event.messenging;

import org.bukkit.Bukkit;
import org.tbstcraft.quark.util.container.ListBuilder;
import org.tbstcraft.quark.util.container.MapBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface Messenger {
    String FETCH_KICK_MESSAGE = "quark:kick-message-fetch";

    static void broadcastMapped(String id, Map<String, Object> properties) {
        Bukkit.getPluginManager().callEvent(new MappedBroadcastEvent(id, properties));
    }

    static void broadcastMapped(String id, Consumer<MapBuilder<String,Object>> propertiesInitializer) {
        Bukkit.getPluginManager().callEvent(new MappedBroadcastEvent(id, propertiesInitializer));
    }

    static void broadcastListed(String id, List<Object> properties) {
        Bukkit.getPluginManager().callEvent(new ListedBroadcastEvent(id, properties));
    }

    static void broadcastListed(String id, Consumer<ListBuilder<Object>> propertiesInitializer) {
        Bukkit.getPluginManager().callEvent(new ListedBroadcastEvent(id, propertiesInitializer));
    }

    static MappedQueryEvent queryMapped(String id, Consumer<Map<String, Object>> propertiesInitializer) {
        MappedQueryEvent event = new MappedQueryEvent(id, propertiesInitializer);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    static ListedQueryEvent queryListed(String id, Consumer<ListBuilder<Object>> propertiesInitializer) {
        ListedQueryEvent event = new ListedQueryEvent(id, propertiesInitializer);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    static String queryKickMessage(String playerName, String defaultMessage, String locale) {
        return Messenger.queryMapped(FETCH_KICK_MESSAGE, (map) -> {
            map.put("default", defaultMessage);
            map.put("player", playerName);
            map.put("locale", locale);
        }).getProperty("message", String.class);
    }
}
