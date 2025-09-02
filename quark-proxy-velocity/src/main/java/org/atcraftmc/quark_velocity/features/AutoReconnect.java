package org.atcraftmc.quark_velocity.features;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import net.kyori.adventure.text.Component;

public class AutoReconnect {
    @Subscribe
    public void onKick(KickedFromServerEvent event) {
        if (event.getServerKickReason().isPresent()) {
            Component reason = event.getServerKickReason().get();
            if (reason.toString().contains("Timeout") || reason.toString().contains("Connection reset")) {

            }




        }
    }
}
