package org.atcraftmc.starlight.internal;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.ComponentLike;
import org.atcraftmc.qlib.platform.ForwardingPluginPlatform;
import org.atcraftmc.qlib.platform.PluginPlatform;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.bukkit.entity.Player;

@SLModule(internal = true, description = "Create more compatible message sending via ProtocolLib.")
public final class ProtocolLibPlatformInjector extends PackageModule {

    @Override
    public void enable() {
        PluginPlatform.global().addAfter("starlight:core", "starlight:plib-inject", new ProtocolLibMessageImpl());
    }

    @Override
    public void disable() {
        PluginPlatform.global().remove("starlight:plib-inject");
    }

    public static class ProtocolLibMessageImpl extends ForwardingPluginPlatform {

        @Override
        public void sendMessage(Object pointer, ComponentLike message) {
            if (!(pointer instanceof Player player)) {
                super.sendMessage(pointer, message);
                return;
            }

            var packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CHAT);
            var component = WrappedChatComponent.fromJson(ComponentSerializer.json(message));

            packet.getChatComponents().write(0, component);

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
