package org.atcraftmc.quark.velocity.peer;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark.velocity.ProxyModule;
import org.atcraftmc.quark.velocity.Registers;

@AutoRegister(Registers.REMOTE_MESSAGE)
public final class BungeeConnectionProtect extends ProxyModule {

    @Subscribe
    public void onServerConnect(ServerPreConnectEvent event) {
        getMessenger().sendMessage(event.getOriginalServer().getServerInfo().getName(), "/bc-connect/add",
                (buffer) -> BufferUtil.writeString(buffer, event.getPlayer().getUsername()));
    }
}
