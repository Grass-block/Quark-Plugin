package org.atcraftmc.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.foundation.region.SimpleRegion;
import org.tbstcraft.quark.framework.event.WorldeditSectionUpdateEvent;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.atcraftmc.quark.security.WESessionTrackService;

@QuarkModule(version = "1.0.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkCommand(name = "render_we_selection")
public final class WorldEditSelectionRenderer extends CommandModule {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        this.render((Player) sender);
    }

    @EventHandler
    public void onSectionUpdate(WorldeditSectionUpdateEvent event) {
        this.render(event.getPlayer());
    }

    public void render(Player p) {
        SimpleRegion r = WESessionTrackService.getRegion(p);
        Players.show3DBox(p, r.getPoint0(), r.getPoint1());
    }
}
