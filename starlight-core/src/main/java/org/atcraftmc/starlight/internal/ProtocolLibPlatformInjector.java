package org.atcraftmc.starlight.internal;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.module.ApplicationModule;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.atcraftmc.qlib.platform.ForwardingPluginPlatform;
import org.atcraftmc.qlib.platform.PluginPlatform;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.entity.Player;

import java.util.UUID;

@ApplicationModule(id = "protocol-lib-injector", internal = true, description = "Create more compatible message sending via ProtocolLib.")
public final class ProtocolLibPlatformInjector extends SLPackageModule {

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requirePlugin("ProtocolLib");
    }

    @Override
    public void enable() {
        PluginPlatform.global().addAfter("starlight:core", "starlight:plib-inject", getImpl());
    }

    @Override
    public void disable() {
        PluginPlatform.global().remove("starlight:plib-inject");
    }

    public ForwardingPluginPlatform getImpl() {
        try {
            PacketType.Play.Server.class.getDeclaredField("SYSTEM_CHAT");
            PacketRegistry.getPacketClassFromType(PacketType.Play.Server.SYSTEM_CHAT);
            return new SystemChatImpl();
        } catch (Throwable e) {
            try {
                Class.forName("com.comphenix.protocol.wrappers.EnumWrappers$ChatType");
                return new ChatTypeImpl();
            } catch (Throwable ee) {
                return new LegacyImpl();
            }
        }
    }


    public static final class SystemChatImpl extends ForwardingPluginPlatform {

        @Override
        public void sendMessage(Object pointer, ComponentLike message) {
            if (!(pointer instanceof Player player)) {
                super.sendMessage(pointer, message);
                return;
            }

            var json = GsonComponentSerializer.gson().serialize(message.asComponent());
            var packet = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);
            packet.getModifier().writeDefaults();
            try {
                packet.getStrings().write(0, json);
            } catch (Exception ex) {
                try {
                    packet.getChatComponents().write(0, com.comphenix.protocol.wrappers.WrappedChatComponent.fromJson(json));
                } catch (Exception ignored) {
                }
            }
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        }
    }

    public static final class ChatTypeImpl extends ForwardingPluginPlatform {

        @Override
        public void sendMessage(Object pointer, ComponentLike message) {
            if (!(pointer instanceof Player player)) {
                super.sendMessage(pointer, message);
                return;
            }

            String json = GsonComponentSerializer.gson().serialize(message.asComponent());

            PacketContainer packet = new PacketContainer(PacketType.Play.Server.CHAT);
            packet.getModifier().writeDefaults();

            packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
            packet.getChatTypes().write(0, EnumWrappers.ChatType.SYSTEM);

            packet.getUUIDs().writeSafely(0, UUID.randomUUID());
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        }
    }

    public static final class LegacyImpl extends ForwardingPluginPlatform {

        @Override
        public void sendMessage(Object pointer, ComponentLike message) {
            if (!(pointer instanceof Player player)) {
                return;
            }

            String json = GsonComponentSerializer.gson().serialize(message.asComponent());
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.CHAT);
            packet.getModifier().writeDefaults();
            packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
            packet.getBytes().write(0, (byte) 1);

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        }
    }

}
