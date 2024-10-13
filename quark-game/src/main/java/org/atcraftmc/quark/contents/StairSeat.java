package org.atcraftmc.quark.contents;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.util.Vector;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.HashMap;
import java.util.Objects;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "2.0.1")
public final class StairSeat extends PackageModule {
    public static final String CHAIR_ENTITY_ID = "quark_chair_entity";
    private final HashMap<String, Entity> entityMapping = new HashMap<>();
    private final HashMap<String, Location> locationMapping = new HashMap<>();
    private final HashMap<Location, String> handledBlocks = new HashMap<>();

    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireMethod(() -> Material.class.getMethod("getKey"));
    }

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
    }

    @Override
    public void disable() {
        for (String s : this.entityMapping.keySet()) {
            this.removePlayerSeat(s);
        }
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (this.handledBlocks.containsKey(block.getLocation())) {
            return;
        }
        if (block.getWorld().getBlockAt(block.getLocation().add(0, 1, 0)).getType() != Material.AIR) {
            return;
        }
        if (!event.getClickedBlock().getType().getKey().getKey().contains("stair")) {
            return;
        }
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) {
            return;
        }
        World world = block.getWorld();
        Location spawnLocation = block.getLocation();

        Vector direction = getStairsFacing(block);
        if (direction == null) {
            return;
        }

        spawnLocation.setDirection(direction.multiply(-1));
        Player player = event.getPlayer();
        var spawn = spawnLocation.add(0.5, -0.36, 0.5);
        Pig pig = (Pig) world.spawnEntity(spawn, EntityType.PIG);

        pig.setInvulnerable(true);
        pig.addScoreboardTag(CHAIR_ENTITY_ID);
        pig.setGravity(false);
        pig.setAI(false);
        pig.setInvisible(true);
        pig.setSilent(true);
        Objects.requireNonNull(pig.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(-1);
        pig.addPassenger(player);

        this.entityMapping.put(player.getName(), pig);
        this.locationMapping.put(player.getName(), block.getLocation());
        this.handledBlocks.put(block.getLocation(), player.getName());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        if (!this.handledBlocks.containsKey(loc)) {
            return;
        }
        this.removePlayerSeat(this.handledBlocks.get(loc));
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        Entity e = event.getExited();
        if (!(e instanceof Player player)) {
            return;
        }
        this.removePlayerSeat(player.getName());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        this.removePlayerSeat(event.getPlayer().getName());
    }

    public void removePlayerSeat(String id) {
        if (!entityMapping.containsKey(id)) {
            return;
        }
        Location loc = this.locationMapping.get(id);
        Entity entity = this.entityMapping.get(id);

        this.locationMapping.remove(id);
        this.entityMapping.remove(id);
        this.handledBlocks.remove(loc);

        entity.remove();
        Player p = Bukkit.getPlayerExact(id);
        if (p == null) {
            return;
        }
        Players.teleport(p, p.getLocation().add(0, 2, 0));
    }

    private Vector getStairsFacing(Block stairsBlock) {
        if (stairsBlock == null) {
            throw new RuntimeException("wtf this is not a stair!");
        }

        BlockData blockData = stairsBlock.getBlockData();

        if (!(blockData instanceof Stairs stairData)) {
            throw new RuntimeException("wtf this is not a stair!");
        }

        if (stairData.getHalf() != Bisected.Half.BOTTOM) {
            return null;
        }

        Vector baseDirection = switch (stairData.getFacing()) {
            case EAST -> new Vector(1, 0, 0);
            case SOUTH -> new Vector(0, 0, 1);
            case WEST -> new Vector(-1, 0, 0);
            default -> new Vector(0, 0, -1);
        };

        Stairs.Shape shape = stairData.getShape();
        int deg = 0;

        if (shape == Stairs.Shape.OUTER_RIGHT || shape == Stairs.Shape.INNER_RIGHT) {
            deg = 45;
        }
        if (shape == Stairs.Shape.OUTER_LEFT || shape == Stairs.Shape.INNER_LEFT) {
            deg = -45;
        }

        double angle = Math.toRadians(deg);
        if (angle == 0) {
            return baseDirection;
        }
        double newX = baseDirection.getX() * Math.cos(angle) - baseDirection.getZ() * Math.sin(angle);
        double newZ = baseDirection.getX() * Math.sin(angle) + baseDirection.getZ() * Math.cos(angle);

        return new Vector(newX, baseDirection.getY(), newZ);
    }
}
