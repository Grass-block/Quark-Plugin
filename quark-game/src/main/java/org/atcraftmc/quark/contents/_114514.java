package org.atcraftmc.quark.contents;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.Registrations;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPistonExtendEvent;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.TaskService;

import java.util.HashMap;

@AutoRegister(Registrations.SERVER_EVENT)
@ApplicationModule(version = "1.0.0", beta = true)
public final class _114514 extends SLPackageModule {
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
                p.chat(this.language.item("message").message(LocaleService.locale(p)));
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
