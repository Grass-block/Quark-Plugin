package org.atcraftmc.quark_velocity.features;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import me.gb2022.commons.math.SHA;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark_velocity.ProxyModule;
import org.atcraftmc.quark_velocity.Registers;

@AutoRegister(Registers.VELOCITY_EVENT)
public final class LegacyForwardingProtect extends ProxyModule {

    @Subscribe
    public void onServerConnect(ServerPreConnectEvent event) {
        var server = event.getOriginalServer().getServerInfo().getName();
        var sign = SHA.getSHA256(event.getPlayer().getUsername(), true);

        this.getMessenger().message(server, "forwarding:verification", sign);
    }
}
