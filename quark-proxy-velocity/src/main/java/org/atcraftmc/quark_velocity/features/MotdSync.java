package org.atcraftmc.quark_velocity.features;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark_velocity.ProxyModule;
import org.atcraftmc.quark_velocity.Registers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@AutoRegister(Registers.VELOCITY_EVENT)
public final class MotdSync extends ProxyModule {
    private ServerPing ping;
    private long lastUpdate;

    @Override
    public void enable() {
        this.update();
    }

    private void update() {
        try {
            var name = getConfig("motd-sync").getString("provider");
            getProxy().getServer(name).ifPresentOrElse((s) -> {
                try {
                    this.ping = s.ping().get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }, () -> getLogger().error("no server motd provider named {}", name));

            this.lastUpdate = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Subscribe
    public void onPing(ProxyPingEvent event) {
        event.setResult(ResultedEvent.GenericResult.allowed());
        var config = getConfig("motd-sync");

        if (System.currentTimeMillis() - this.lastUpdate > config.getInt("update-interval") * 1000L){
            this.update();
        }

        ServerPing origin = event.getPing();

        int max = config.getInt("max-players");

        List<ServerPing.SamplePlayer> samples = new ArrayList<>();

        if (!config.getBoolean("hide-players")) {
            for (var p : this.getProxy().getAllPlayers()) {
                samples.add(new ServerPing.SamplePlayer(p.getUsername(), p.getUniqueId()));
            }
        }

        var players = new ServerPing.Players(this.getProxy().getPlayerCount(), max, samples);
        var favicon = this.ping.getFavicon().orElse(null);

        event.setPing(new ServerPing(origin.getVersion(), players, this.ping.getDescriptionComponent(), favicon));
    }
}
