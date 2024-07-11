package org.tbstcraft.quark.utilities;

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
import org.tbstcraft.quark.data.PlaceHolderStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.HashSet;
import java.util.List;

@QuarkModule(version = "1.0")
@QuarkCommand(name = "refresh-area", permission = "-quark.refresh.command", playerOnly = true)
@AutoRegister(ServiceType.PLUGIN_MESSAGE)
public final class SurroundingRefresh extends CommandModule {

    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PlaceHolderStorage.get(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, HashSet.class, (s) -> s.add(this.tip));
        super.enable();
    }

    @Override
    public void disable(){
        PlaceHolderStorage.get(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, HashSet.class, (s) -> s.remove(this.tip));
        super.disable();
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        int rad = Integer.parseInt(args[0]);

        if (rad > 9) {
            getLanguage().sendMessage(sender, "to-big", rad);
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
            target = PlayerUtil.strictFindPlayer(args[1]);
            if (target == null) {
                getLanguage().sendMessage(sender, "not-found");
                return;
            }
            getLanguage().sendMessage(sender, "success", rad);
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
            tabList.addAll(PlayerUtil.getAllOnlinePlayerNames());
        }
    }

    @PluginMessageHandler("quark:explosion")
    public void onPluginMessage(MappedBroadcastEvent event) {
        Location loc = event.getProperty("loc", Location.class);

        for (Player p : Bukkit.getOnlinePlayers()) {
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
            getLanguage().sendMessage(p, "refresh-target", radius);
        }

        int delay = 0;

        for (int xx = x - radius; xx <= x + radius; xx++) {
            for (int yy = y - radius; yy <= y + radius; yy++) {
                for (int zz = z - radius; zz <= z + radius; zz++) {
                    Location loc = new Location(p.getWorld(), xx, yy, zz);
                    if (!silent) {
                        TaskService.laterTask(delay, () -> {
                            p.sendBlockChange(loc, Material.AIR.createBlockData());
                        });

                        TaskService.laterTask(delay + 5, () -> {
                            p.sendBlockChange(loc, loc.getBlock().getBlockData());
                        });
                    } else {
                        p.sendBlockChange(loc, Material.AIR.createBlockData());
                        p.sendBlockChange(loc, loc.getBlock().getBlockData());
                    }
                }
            }
            delay++;
        }

        if (!silent) {
            getLanguage().sendMessage(p, "refresh-complete");
        }
    }
}
