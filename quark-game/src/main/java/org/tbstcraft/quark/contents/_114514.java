package org.tbstcraft.quark.contents;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.service.base.task.TaskService;
import org.tbstcraft.quark.util.api.PlayerUtil;

import java.util.HashMap;

@EventListener
@QuarkModule(version = "1.0.0", beta = true)
public final class _114514 extends PackageModule {
    private final HashMap<String, Integer> maps = new HashMap<>();
    private final HashMap<String, Long> timestamps = new HashMap<>();

    @EventHandler
    public void onPistonUpdate(BlockPistonExtendEvent event) {
        TaskService.runDelayTask(event.getBlock().getLocation(), 3, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getLocation().getBlock().getType() != Material.END_ROD) {
                    continue;
                }
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_SLIME_BLOCK_HIT, 2.0f, 1);

                if (!shouldChat(p)) {
                    continue;
                }
                p.chat(this.getLanguage().getRandomMessage(PlayerUtil.getLocale(p), "message"));
            }
        });
    }

    private boolean shouldChat(Player p) {
        String name = p.getName();
        if (!this.maps.containsKey(name)) {
            this.maps.put(name, 0);
            this.timestamps.put(name, System.currentTimeMillis());
            return true;
        } else {
            long time = this.timestamps.get(name);
            if (System.currentTimeMillis() - time > 700) {
                this.timestamps.put(name, System.currentTimeMillis());
                this.maps.put(name, 0);
                return true;
            } else {
                this.timestamps.put(name, System.currentTimeMillis());
                this.maps.put(name, this.maps.get(name) + 1);
                return this.maps.get(name) % 6 == 0;
            }
        }
    }
}