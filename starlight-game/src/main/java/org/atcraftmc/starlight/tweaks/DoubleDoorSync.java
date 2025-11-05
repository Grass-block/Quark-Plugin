package org.atcraftmc.starlight.tweaks;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

@ApplicationModule(id = "double-door-sync", version = "1.0.0")
@AutoRegister(Registrations.SERVER_EVENT)
public final class DoubleDoorSync extends SLPackageModule {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || isNotWoodenDoor(clickedBlock.getType())) {
            return;
        }
        checkDoors(clickedBlock);
    }

    private void checkDoors(Block dest) {
        var d = dest.getBlockData();

        if (!(d instanceof Door data)) {
            return;
        }

        var destFace = switch (data.getFacing()) {
            case WEST -> BlockFace.SOUTH;
            case EAST -> BlockFace.NORTH;
            case NORTH -> BlockFace.WEST;
            case SOUTH -> BlockFace.EAST;
            default -> throw new IllegalStateException("Unexpected value: " + data.getFacing());
        };
        if (data.getHinge() == Door.Hinge.LEFT) {
            destFace = destFace.getOppositeFace();
        }

        var pair = dest.getRelative(destFace);
        if (isNotWoodenDoor(pair.getType())) {
            return;
        }

        if (!(pair.getBlockData() instanceof Door pairData)) {
            return;
        }

        if (pairData.getHinge() == data.getHinge()) {
            return;
        }
        if (pairData.getHalf() != data.getHalf()) {
            return;
        }

        pairData.setOpen(!data.isOpen());
        pair.setBlockData(pairData);

        var pairHalf = pair.getRelative(pairData.getHalf() == Bisected.Half.BOTTOM ? BlockFace.UP : BlockFace.DOWN);
        var pairHalfData = (Door) pairHalf.getBlockData();
        pairHalfData.setOpen(!data.isOpen());
        pairHalf.setBlockData(pairHalfData);
    }

    private boolean isNotWoodenDoor(Material material) {
        return !material.getKey().getKey().contains("_door");
    }
}
