package org.tbstcraft.quark.internal.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class DirectAudienceProvider extends AudienceProvider {
    @Override
    public Audience player(Player player) {
        return player;
    }

    @Override
    public Audience console() {
        return Bukkit.getConsoleSender();
    }

    @Override
    public ForwardingAudience players(Collection<? extends Player> players) {
        return Audience.audience(players);
    }
}
