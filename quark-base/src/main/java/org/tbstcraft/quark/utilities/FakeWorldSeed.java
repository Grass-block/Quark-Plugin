package org.tbstcraft.quark.utilities;

/*
import io.wdsj.asg.libs.packetevents.impl.PacketEvents;
import io.wdsj.asg.libs.packetevents.impl.event.PacketListenerAbstract;
import io.wdsj.asg.libs.packetevents.impl.event.PacketListenerPriority;
import io.wdsj.asg.libs.packetevents.impl.event.PacketSendEvent;
import io.wdsj.asg.libs.packetevents.impl.protocol.packettype.PacketTypeCommon;
import io.wdsj.asg.libs.packetevents.impl.wrapper.play.server.WrapperPlayServerJoinGame;
import io.wdsj.asg.libs.packetevents.impl.wrapper.play.server.WrapperPlayServerRespawn;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.framework.service.base.ProductService;


@QuarkModule(version = "1.0.0")
public class FakeWorldSeed extends PackageModule {
    static long hashWorldSeed() {
        String name = ProductService.getSystemIdentifier();
        int h1 = name.substring(0, 16).hashCode();
        int h2 = name.substring(16).hashCode();
        return (h1 * 2147483647L + h2) & 1145141919810L;
    }

    @Override
    public void enable() {
        PacketEvents.getAPI().getEventManager().registerListener(new WorldSeedListener());
    }

    public static final class WorldSeedListener extends PacketListenerAbstract {
        public WorldSeedListener() {
            super(PacketListenerPriority.NORMAL);
        }

        public void onPacketSend(PacketSendEvent event) {
            PacketTypeCommon packetType = event.getPacketType();
            Player player = (Player) event.getPlayer();

            if (packetType == io.wdsj.asg.libs.packetevents.impl.protocol.packettype.PacketType.Play.Server.JOIN_GAME) {
                System.out.println("awa");

                if (player == null) {
                    return;
                }

                WrapperPlayServerJoinGame wrapper = new WrapperPlayServerJoinGame(event);
                wrapper.setHashedSeed(hashWorldSeed());
            } else if (packetType == io.wdsj.asg.libs.packetevents.impl.protocol.packettype.PacketType.Play.Server.RESPAWN) {

                WrapperPlayServerRespawn wrapper = new WrapperPlayServerRespawn(event);
                wrapper.setHashedSeed(hashWorldSeed());
            }
        }
    }
}


 */