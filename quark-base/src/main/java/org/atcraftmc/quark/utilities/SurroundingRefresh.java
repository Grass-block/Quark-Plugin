package org.atcraftmc.quark.utilities;

import me.gb2022.apm.local.MappedBroadcastEvent;
import me.gb2022.apm.local.PluginMessageHandler;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.List;

@QuarkModule
@QuarkCommand(name = "refresh-area", permission = "-quark.refresh.command", playerOnly = true)
@AutoRegister(ServiceType.PLUGIN_MESSAGE)
public final class SurroundingRefresh extends CommandModule {

    @Inject
    private LanguageEntry language;
    
    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
        super.enable();
    }

    @Override
    public void disable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
        super.disable();
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        int rad = Integer.parseInt(args[0]);

        if (rad > 9) {
            this.language.sendMessage(sender, "to-big", rad);
            return;
        }

        Player player = (Player) sender;

        Player target;

        if (args.length == 1) {
            target = player;
        } else {
            if (!sender.isOp()) {
                sendPermissionMessage(sender);
                return;
            }
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                this.language.sendMessage(sender, "not-found");
                return;
            }
            this.language.sendMessage(sender, "success", rad);
        }

        refreshArea(target, rad, false);
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("3");
            tabList.add("5");
            tabList.add("7");
            tabList.add("9");
        }
        if (buffer.length == 2) {
            if (!sender.isOp()) {
                return;
            }
            tabList.addAll(Players.getAllOnlinePlayerNames());
        }
    }

    @PluginMessageHandler("quark:explosion")
    public void onPluginMessage(MappedBroadcastEvent event) {
        Location loc = event.getProperty("loc", Location.class);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld() != loc.getWorld()) {
                continue;
            }

            if (loc.distance(p.getLocation()) < 64) {
                refreshArea(p, 7, true);
            }
        }
    }

    public void refreshArea(Player p, int radius, boolean silent) {
        int x = p.getLocation().getBlockX();
        int y = p.getLocation().getBlockY();
        int z = p.getLocation().getBlockZ();

        if (!silent) {
            this.language.sendMessage(p, "refresh-target", radius);
        }

        int delay = 0;

        for (int xx = x - radius; xx <= x + radius; xx++) {
            for (int yy = y - radius; yy <= y + radius; yy++) {
                for (int zz = z - radius; zz <= z + radius; zz++) {
                    Location loc = new Location(p.getWorld(), xx, yy, zz);
                    if (!silent) {
                        TaskService.global().delay(delay, () -> p.sendBlockChange(loc, Material.AIR.createBlockData()));

                        TaskService.global().delay(delay + 5, () -> p.sendBlockChange(loc, loc.getBlock().getBlockData()));
                    } else {
                        p.sendBlockChange(loc, Material.AIR.createBlockData());
                        p.sendBlockChange(loc, loc.getBlock().getBlockData());
                    }
                }
            }
            delay++;
        }

        if (!silent) {
            this.language.sendMessage(p, "refresh-complete");
        }
    }
}
