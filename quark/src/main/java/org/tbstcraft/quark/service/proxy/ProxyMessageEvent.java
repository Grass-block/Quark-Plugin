package org.tbstcraft.quark.service.proxy;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ProxyMessageEvent extends Event {
    private static final HandlerList handlers=new HandlerList();

    private final ProxyChannel channel;
    private final byte[] message;

    public ProxyMessageEvent(ProxyChannel channel, byte[] message) {
        this.channel = channel;
        this.message = message;
    }

    public byte[] getMessage() {
        return message;
    }

    public ProxyChannel getChannel() {
        return channel;
    }

    public boolean checkChannelId(String id){
        return id==this.getChannel().getId();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList(){
        return handlers;
    }
}
