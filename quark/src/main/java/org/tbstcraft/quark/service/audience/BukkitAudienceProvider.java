package org.tbstcraft.quark.service.audience;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.tbstcraft.quark.Quark;


public class BukkitAudienceProvider extends WrappedAudienceProvider{
    public BukkitAudienceProvider() {
        super(BukkitAudiences.create(Quark.PLUGIN));
    }
}
