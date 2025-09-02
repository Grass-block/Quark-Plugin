package org.atcraftmc.quark_velocity.features;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark_velocity.Config;
import org.atcraftmc.quark_velocity.ProxyModule;
import org.atcraftmc.quark_velocity.Registers;

@AutoRegister(Registers.VELOCITY_EVENT)
public final class ModdedServerSupport extends ProxyModule {

    @Subscribe
    public void onServerConnect(ServerPreConnectEvent event) {
        var player = event.getPlayer();
        var mods = getConfig("mod-server-support");
        var sid = event.getResult().getServer().map((s) -> s.getServerInfo().getName()).orElse("__unknown__");

        if (sid.equals("__unknown__")) {
            return;
        }

        var mod = mods.get(sid);

        if (mod == null || mod == "null") {
            return;
        }

        var mis = player.getModInfo();

        if (mis.isPresent()) {
            if (mis.get().getMods().stream().anyMatch((m) -> {
                //System.out.println(mod + " - " + m.getId());
                return m.getId().equals(mod);
            })) {
                return;
            }
        }

        event.setResult(ServerPreConnectEvent.ServerResult.denied());

        Config.language("mod-server-support").sendMessage(player, "message");
    }
}
