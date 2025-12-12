package org.atcraftmc.starlight.commands;

import me.gb2022.commons.reflect.Inject;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO1;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.assertion.NumberLimitation;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.starlight.api.customization.CustomBlock;
import org.atcraftmc.starlight.api.customization.CustomItem;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.custom.CustomBlockService;
import org.atcraftmc.starlight.framework.module.SLCommandModule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationModule(id = "animate-block-command")
@QuarkCommand(name = "animate-block")
public final class AnimateBlockCommand extends SLCommandModule {
    @SuppressWarnings("Convert2MethodRef")
    static MethodHandleO1<FallingBlock, Boolean> SET_PHYSICS = MethodHandle.select((c) -> {
        c.attempt(() -> FallingBlock.class.getMethod("setNoPhysics", boolean.class), (e, a) -> e.setNoPhysics(a));
        c.dummy((e, a) -> {});
    });

    static MethodHandleO1<Block, Material> SET_TYPE = MethodHandle.select((c) -> {
        c.attempt(() -> Block.class.getMethod("setType", Material.class, boolean.class), (e, a) -> e.setType(a, false));
        c.dummy(Block::setType);
    });

    static MethodHandleO1<Block, BlockData> SET_BLOCK_DATA = MethodHandle.select((c) -> {
        c.attempt(() -> Block.class.getMethod("setBlockData", BlockData.class, boolean.class), (e, a) -> e.setBlockData(a, false));
        c.dummy(Block::setBlockData);
    });

    private final HelperWandItem item = new HelperWandItem();
    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() throws Exception{
        super.enable();
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
        CustomBlockService.instance().registerItem(this.item);
    }

    @Override
    public void disable() throws Exception{
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
        CustomBlockService.instance().unregisterItem("animate-block-helper");
        super.disable();
    }


    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "[time]");
        suggestion.suggest(1, "[x0]");
        suggestion.suggest(2, "[y0]");
        suggestion.suggest(3, "[z0]");
        suggestion.suggest(4, "[x1]");
        suggestion.suggest(5, "[y1]");
        suggestion.suggest(6, "[z1]");
        suggestion.suggest(7, "[targetX]");
        suggestion.suggest(8, "[targetY]");
        suggestion.suggest(9, "[targetZ]");
    }

    @Override
    public void execute(CommandExecution context) {
        var ticks = context.requireArgumentInteger(0, NumberLimitation.moreThan(0));
        var first = context.requireCoordinate(1);
        var end = context.requireCoordinate(4);
        var target = context.requireCoordinate(7);

        var x0 = (int) Math.floor(first.getX());
        var y0 = (int) Math.floor(first.getY());
        var z0 = (int) Math.floor(first.getZ());
        var x1 = (int) Math.floor(end.getX());
        var y1 = (int) Math.floor(end.getY());
        var z1 = (int) Math.floor(end.getZ());
        var tx = (int) Math.floor(target.getX());
        var ty = (int) Math.floor(target.getY());
        var tz = (int) Math.floor(target.getZ());

        if (x0 > x1) {
            this.language().item("size-invalid").send(context.getSender(), "x");
            return;
        }
        if (y0 > y1) {
            this.language().item("size-invalid").send(context.getSender(), "y");
            return;
        }
        if (z0 > z1) {
            this.language().item("size-invalid").send(context.getSender(), "z");
            return;
        }

        if (x1 - x0 > 16) {
            this.language().item("size-too-big").send(context.getSender(), 8);
            return;
        }
        if (y1 - y0 > 16) {
            this.language().item("size-too-big").send(context.getSender(), 8);
            return;
        }
        if (z1 - z0 > 16) {
            this.language().item("size-too-big").send(context.getSender(), 8);
            return;
        }

        var entities = new ArrayList<FallingEntityAgent>();
        var loc = context.attemptGetSenderLocation();
        var world = loc.getWorld();

        for (int i = x0; i <= x1; i++) {
            for (int j = y1; j >= y0; j--) {
                for (int k = z0; k <= z1; k++) {
                    var block = world.getBlockAt(i, j, k);
                    var e = FallingEntityAgent.create(block, x0, y0, z0);

                    if (e != null) {
                        entities.add(e);
                    }
                }
            }
        }

        if (entities.isEmpty()) {
            this.language().item("no-result").send(context.getSender());
            return;
        }

        for (var e : entities) {
            e.dismount();
        }

        this.language().item("success").send(context.getSender(), entities.size());

        var dx = tx - x0;
        var dy = ty - y0;
        var dz = tz - z0;

        var vx = (float) dx / ticks;
        var vy = (float) dy / ticks;
        var vz = (float) dz / ticks;

        var vs = new Vector(x0, y0, z0);
        var ve = new Vector(tx, ty, tz);

        var v = new Vector(vx, vy, vz);

        var timer = new AtomicInteger();

        TaskService.entity(entities.get(0).block).timer(0, 1, (t) -> {
            if (timer.get() > ticks) {
                for (var entity : entities) {
                    entity.block.setVelocity(new Vector(0, 0, 0));
                }
                t.cancel();
                return;
            }

            for (var entity : entities) {
                entity.tickPosition(vs, ve, v, timer.get(), ticks);
            }

            timer.getAndIncrement();
        });

        TaskService.region(entities.get(0).block.getLocation()).delay(ticks, () -> {
            var location = new Location(world, tx, ty, tz);

            for (var i = entities.size() - 1; i >= 0; i--) {
                entities.get(i).toBlock(location);
            }

            entities.clear();
        });
    }

    private static final class FallingEntityAgent {
        private final FallingBlock block;
        private final Block previous;
        private final int relX;
        private final int relY;
        private final int relZ;

        private FallingEntityAgent(FallingBlock block, Block previous, int relX, int relY, int relZ) {
            this.block = block;
            this.previous = previous;
            this.relX = relX;
            this.relY = relY;
            this.relZ = relZ;
        }

        public static FallingEntityAgent create(Block block, int x0, int y0, int z0) {
            var rx = block.getX() - x0;
            var ry = block.getY() - y0;
            var rz = block.getZ() - z0;

            if (block.getType().isAir()) {
                return null;
            }

            var data = block.getBlockData();
            var entity = block.getWorld().spawnFallingBlock(new Location(
                    block.getWorld(),
                    block.getX() + 0.5,
                    block.getY(),
                    block.getZ() + 0.5
            ), data);

            entity.setGravity(false);
            entity.setFallDistance(0);
            entity.setInvulnerable(true);
            entity.setDropItem(false);

            SET_PHYSICS.invoke(entity, true);

            return new FallingEntityAgent(entity, block, rx, ry, rz);
        }

        public void tickPosition(Vector start, Vector end, Vector speed, int timer, int tick) {
            var sax = start.getBlockX() + 0.5;
            var saz = start.getBlockZ() + 0.5;
            var eax = end.getBlockX() + 0.5;
            var eaz = end.getBlockZ() + 0.5;

            var delta = ((float) timer + 1) / tick;
            var ex = sax + (eax - sax) * delta + relX;
            var ey = start.getBlockY() + (end.getBlockY() - start.getBlockY()) * delta + relY;
            var ez = saz + (eaz - saz) * delta + relZ;

            var eloc = new Location(this.block.getWorld(), ex, ey, ez);

            if (eloc.distance(this.block.getLocation()) > 0.05) {
                this.block.teleport(eloc);
            }

            this.block.setVelocity(speed);
        }

        public void dismount() {
            SET_TYPE.invoke(this.previous, Material.AIR);
        }

        public void toBlock(Location target) {
            var x0 = target.getBlockX();
            var y0 = target.getBlockY();
            var z0 = target.getBlockZ();
            var world = target.getWorld();

            var b = world.getBlockAt(x0 + this.relX, y0 + this.relY, z0 + this.relZ);
            SET_BLOCK_DATA.invoke(b, this.block.getBlockData());
            this.block.remove();
        }

    }

    public final class HelperWandItem extends CustomItem {

        private HelperWandItem() {
            super("animate-block-helper");
        }

        @Override
        public LanguageItem getDisplayName(ItemStack stack) {
            return language().item("item-name");
        }

        @Override
        public LanguageItem getDescription(ItemStack stack) {
            return language().item("item-lore");
        }

        @Override
        public Material getActualBlock() {
            return Material.STICK;
        }
    }
}
