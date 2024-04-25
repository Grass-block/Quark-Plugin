package org.tbstcraft.quark.display;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.event.WorldeditSectionUpdateEvent;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.service.WESessionTrackService;
import org.tbstcraft.quark.util.api.PlayerUtil;
import org.tbstcraft.quark.util.Region;

@QuarkModule(version = "1.0.0")
@EventListener
@QuarkCommand(name = "render_we_selection")
public class WorldEditSelectionRenderer extends CommandModule {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        this.render((Player) sender);
    }

    @EventHandler
    public void onSectionUpdate(WorldeditSectionUpdateEvent event) {
        this.render(event.getPlayer());
    }

    public void render(Player p) {
        Region r = WESessionTrackService.getRegion(p);
        PlayerUtil.show3DBox(p, r.getPoint0(), r.getPoint1());
    }
}
