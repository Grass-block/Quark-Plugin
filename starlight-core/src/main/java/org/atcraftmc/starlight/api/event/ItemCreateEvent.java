package org.atcraftmc.starlight.api.event;


import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;
import org.bukkit.entity.Item;
import org.bukkit.event.HandlerList;

@SLEvent
public final class ItemCreateEvent extends CustomEvent {
    private final Item item;

    public ItemCreateEvent(Item item) {
        this.item = item;
    }

    public static HandlerList getHandlerList() {
        return getHandlerList(ItemCreateEvent.class);
    }

    public Item getItem() {
        return item;
    }
}
