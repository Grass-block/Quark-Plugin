package org.tbstcraft.quark.deprecated.audience_service;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.tbstcraft.quark.Quark;


public class BukkitAudienceProvider extends WrappedAudienceProvider{
    public BukkitAudienceProvider() {
        super(BukkitAudiences.create(Quark.PLUGIN));
    }
}
