package org.tbstcraft.quark.service;

/*
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import io.wdsj.asg.libs.packetevents.api.factory.spigot.SpigotPacketEventsBuilder;
import io.wdsj.asg.libs.packetevents.impl.PacketEvents;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.util.ObjectContainer;

import java.util.function.Consumer;

public interface ProtocolLibService extends Service {
    ObjectContainer<ProtocolManager> PROTOCOL_MANAGER = new ObjectContainer<>();

    static ProtocolManager getProtocolManager() {
        return PROTOCOL_MANAGER.get();
    }

    static void registerListener(Consumer<PacketEvent> listener, PacketType... types) {
        getProtocolManager().addPacketListener(new PacketAdapter(Quark.PLUGIN, ListenerPriority.NORMAL, types) {
            @Override
            public void onPacketSending(PacketEvent event) {
                listener.accept(event);
            }
        });
    }

    static void init() {
        PROTOCOL_MANAGER.set(ProtocolLibrary.getProtocolManager());
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(Quark.PLUGIN));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(true).checkForUpdates(false).bStats(false);
        PacketEvents.getAPI().load();
    }
}

 */
