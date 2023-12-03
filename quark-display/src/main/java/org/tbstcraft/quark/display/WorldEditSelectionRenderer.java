package org.tbstcraft.quark.display;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.event.WorldeditSectionUpdateEvent;
import org.tbstcraft.quark.module.CommandModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.service.WorldEditLocalSessionTracker;
import org.tbstcraft.quark.util.BukkitUtil;
import org.tbstcraft.quark.util.Region;

import java.util.List;

@QuarkModule
@QuarkCommand(name = "render_we_selection")
public class WorldEditSelectionRenderer extends CommandModule {
    @Override
    public void onEnable() {
        super.onEnable();
        this.registerListener();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.unregisterListener();
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        this.render((Player) sender);
        return true;
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {

    }

    @EventHandler
    public void onSectionUpdate(WorldeditSectionUpdateEvent event){
        this.render(event.getPlayer());
    }

    public void render(Player p) {
        Region r = WorldEditLocalSessionTracker.LOCALIZED_SESSIONS.get(p.getName());
        BukkitUtil.show3DBox(p, r.getPoint0(), r.getPoint1());
    }
}
