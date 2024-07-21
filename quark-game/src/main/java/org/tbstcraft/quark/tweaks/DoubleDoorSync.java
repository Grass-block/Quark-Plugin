package org.tbstcraft.quark.tweaks;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

@QuarkModule(version = "1.0.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class DoubleDoorSync extends PackageModule {
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
        Door data = (Door) dest.getBlockData();

        BlockFace destFace = switch (data.getFacing()) {
            case WEST -> BlockFace.SOUTH;
            case EAST -> BlockFace.NORTH;
            case NORTH -> BlockFace.WEST;
            case SOUTH -> BlockFace.EAST;
            default -> throw new IllegalStateException("Unexpected value: " + data.getFacing());
        };
        if (data.getHinge() == Door.Hinge.LEFT) {
            destFace = destFace.getOppositeFace();
        }

        Block pair = dest.getRelative(destFace);
        if (isNotWoodenDoor(pair.getType())) {
            return;
        }
        Door pairData = ((Door) pair.getBlockData());

        if (pairData.getHinge() == data.getHinge()) {
            return;
        }
        if (pairData.getHalf() != data.getHalf()) {
            return;
        }

        pairData.setOpen(!data.isOpen());
        pair.setBlockData(pairData);

        Block pairHalf = pair.getRelative(pairData.getHalf() == Bisected.Half.BOTTOM ? BlockFace.UP : BlockFace.DOWN);
        Door pairHalfData = (Door) pairHalf.getBlockData();
        pairHalfData.setOpen(!data.isOpen());
        pairHalf.setBlockData(pairHalfData);
    }

    private boolean isNotWoodenDoor(Material material) {
        return !material.getKey().getKey().contains("_door");
    }
}
