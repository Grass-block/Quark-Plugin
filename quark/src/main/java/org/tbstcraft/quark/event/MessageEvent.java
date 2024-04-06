package org.tbstcraft.quark.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class MessageEvent extends Event {
    public static final HandlerList handlerList = new HandlerList();
    private final String message;
    private final HashMap<String, String> params;

    public MessageEvent(String message, HashMap<String, String> params) {
        super(true);
        this.message = message;
        this.params = params;
    }

    @SuppressWarnings({"SameReturnValue", "unused"})
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public static MessageEventBuilder builder(String msg) {
        return new MessageEventBuilder(msg);
    }

    @SuppressWarnings({"unused"})
    public HashMap<String, String> getParams() {
        return params;
    }

    public String getParam(String key) {
        return this.params.get(key);
    }

    public String getMessage() {
        return message;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static final class MessageEventBuilder {
        private final String message;
        private final HashMap<String, String> params = new HashMap<>();


        public MessageEventBuilder(String message) {
            this.message = message;
        }

        public MessageEventBuilder param(String key, String value) {
            this.params.put(key, value);
            return this;
        }

        public MessageEvent build() {
            return new MessageEvent(this.message, this.params);
        }
    }
}
