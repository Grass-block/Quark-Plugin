package org.atcraftmc.quark.contents;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.HashMap;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.0.0", beta = true)
public final class _114514 extends PackageModule {
    private final HashMap<String, Integer> maps = new HashMap<>();
    private final HashMap<String, Long> timestamps = new HashMap<>();

    @EventHandler
    public void onPistonUpdate(BlockPistonExtendEvent event) {
        TaskService.region(event.getBlock().getLocation()).delay(3, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getLocation().getBlock().getType() != Material.END_ROD) {
                    continue;
                }
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_SLIME_BLOCK_HIT, 2.0f, 1);

                if (!shouldChat(p)) {
                    continue;
                }
                p.chat(this.getLanguage().getRandomMessage(Language.locale(p), "message"));
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
