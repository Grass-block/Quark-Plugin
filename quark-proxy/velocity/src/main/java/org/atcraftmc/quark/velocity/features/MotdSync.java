package org.atcraftmc.quark.velocity.features;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark.velocity.ProxyModule;
import org.atcraftmc.quark.velocity.Registers;

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
            String name = getConfig("ping").getString("provider");
            getServer().getServer(name).ifPresentOrElse((s) -> {
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

        if (System.currentTimeMillis() - this.lastUpdate > getConfig("ping").getLong("update-interval") * 1000) {
            this.update();
        }

        ServerPing origin = event.getPing();

        int max = Math.toIntExact(this.getConfig("ping").getLong("max"));

        List<ServerPing.SamplePlayer> samples = new ArrayList<>();

        if (!this.getConfig("ping").getBoolean("hide-players")) {
            for (Player p : this.getServer().getAllPlayers()) {
                samples.add(new ServerPing.SamplePlayer(p.getUsername(), p.getUniqueId()));
            }
        }

        ServerPing.Players players = new ServerPing.Players(this.getServer().getPlayerCount(), max, samples);
        Favicon favicon = this.ping.getFavicon().orElse(null);

        event.setPing(new ServerPing(origin.getVersion(), players, this.ping.getDescriptionComponent(), favicon));
    }
}
