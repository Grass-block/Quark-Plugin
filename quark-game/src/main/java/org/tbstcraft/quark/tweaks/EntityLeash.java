package org.tbstcraft.quark.tweaks;

import me.gb2022.commons.container.MultiMap;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.service.base.task.TaskService;

import java.util.HashSet;
import java.util.Objects;

@QuarkModule(version = "0.3", beta = true)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class EntityLeash extends PackageModule {
    public static final String VIRTUAL_ENTITY_ID = "quark:ve:leash";
    private final MultiMap<LivingEntity, LivingEntity> virtualHolders = new MultiMap<>();
    private final MultiMap<Player, LivingEntity> playerHolders = new MultiMap<>();

    @Override
    public void enable() {
        TaskService.timerTask("quark:leash:tick", 2, 2, () -> {
            for (Entity e : new HashSet<>(this.virtualHolders.keySet())) {
                LivingEntity target = this.virtualHolders.get(e);

                try {
                    target.teleportAsync(e.getLocation());
                } catch (NoSuchMethodError ex) {
                    target.teleport(e.getLocation());
                }
            }
        });
    }

    @Override
    public void disable() {
        for (Entity e : new HashSet<>(this.virtualHolders.keySet())) {
            e.remove();
        }
        this.virtualHolders.clear();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getPlayer().isSneaking()) {
            return;
        }
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.LEAD) {
            return;
        }
        this.unleash(this.playerHolders.get(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.LEAD) {
            return;
        }
        if (!(event.getRightClicked() instanceof LivingEntity entity)) {
            return;
        }
        if (this.virtualHolders.containsKey(entity)) {
            return;
        }
        this.leash(entity, player);
    }

    @EventHandler
    public void onEntityRemove(EntityDeathEvent event) {
        this.unleash(this.virtualHolders.of(event.getEntity()));
    }

    @EventHandler
    public void onEntityUnleash(EntityUnleashEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity pig)) {
            return;
        }
        this.unleash(pig);
    }

    public void unleash(LivingEntity virtual) {
        if (!this.virtualHolders.containsKey(virtual)) {
            return;
        }
        virtual.remove();
        this.playerHolders.remove(this.playerHolders.of(virtual));
        this.virtualHolders.remove(virtual);
    }

    public void leash(LivingEntity target, Player holder) {
        if (this.virtualHolders.containsKey(target)) {
            return;
        }
        Location spawnLocation = target.getLocation();
        Pig pig = (Pig) target.getWorld().spawnEntity(spawnLocation.add(0.5, 0, 0.5), EntityType.PIG);

        pig.setInvulnerable(true);
        pig.addScoreboardTag(VIRTUAL_ENTITY_ID);
        pig.setInvisible(true);
        pig.setSilent(true);
        Objects.requireNonNull(pig.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(-1);
        this.virtualHolders.put(pig, target);
        pig.setLeashHolder(holder);
    }
}
