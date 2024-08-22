package org.tbstcraft.quark.deprecated.audience_service;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WrappedAudienceProvider extends AudienceProvider {
    private final net.kyori.adventure.platform.AudienceProvider provider;

    public WrappedAudienceProvider(net.kyori.adventure.platform.AudienceProvider provider) {
        this.provider = provider;
    }

    @Override
    public Audience player(Player player) {
        return this.provider.player(player.getUniqueId());
    }

    @Override
    public Audience console() {
        return this.provider.console();
    }

    @Override
    public ForwardingAudience players(Collection<? extends Player> players) {
        List<Audience> audiences = new ArrayList<>();

        for (Player p : players) {
            audiences.add(player(p));
        }

        return Audience.audience(audiences);
    }

    @Override
    public void close() {
        this.provider.close();
    }
}
