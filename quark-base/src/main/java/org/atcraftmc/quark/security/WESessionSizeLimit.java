package org.atcraftmc.quark.security;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.quark.security.event.WEAction;
import org.atcraftmc.quark.security.event.WESessionEditEvent;
import org.atcraftmc.quark.security.event.WESessionPreEditEvent;
import org.atcraftmc.quark.security.event.WESessionSelectEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("DuplicatedCode")
@QuarkModule(version = "1.2.5", recordFormat = {"Time", "Player", "Action", "X", "Y", "Z", "Limit"})
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class WESessionSizeLimit extends PackageModule {

    @Inject("!quark.we.size.bypass")
    public Permission bypassCheck;

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requirePlugin("WorldEdit");
    }

    @EventHandler
    public void onSelect(WESessionPreEditEvent event) {
        var limit = this.getConfig().getInt("max-selection-size");
        var box = event.getRegion().asAABB();

        var x = box.x1 - box.x0;
        var y = box.y1 - box.y0;
        var z = box.z1 - box.z0;

        if (x <= limit && y <= limit && z <= limit) {
            return;
        }

        var player = event.getPlayer();
        if (player.hasPermission(this.bypassCheck)) {
            if (event.getStage() == EditSession.Stage.BEFORE_CHANGE) {
                this.getLanguage().sendMessage(player, "select-limited-warn", x, y, z, limit);
            }
            return;
        }

        if (event.getStage() == EditSession.Stage.BEFORE_CHANGE) {
            this.getLanguage().sendMessage(player, "select-limited", x, y, z, limit);
        }

        event.setCancelled(true);
        this.getRecord().addLine(SharedObjects.DATE_FORMAT.format(new Date()), "Select", x, y, z, limit);
    }

    @EventHandler
    public void onSelect(WESessionSelectEvent event) {
        var box = event.getRegion().asAABB();
        var limit = this.getConfig().getInt("max-selection-size");

        var x = box.x1 - box.x0;
        var y = box.y1 - box.y0;
        var z = box.z1 - box.z0;

        if (x <= limit && y <= limit && z <= limit) {
            return;
        }

        this.getLanguage().sendMessage(event.getPlayer(), "select-limited-warn", x, y, z, limit);
    }

    @EventHandler
    public void onEdit(WESessionEditEvent event) {
        if (WESessionTrackService.getLatestAction(event.getPlayer()) != WEAction.STACK) {
            return;
        }

        var player = event.getPlayer();
        var cancel = !player.hasPermission(this.bypassCheck);
        var limit = this.getConfig().getInt("max-edit-size");

        var region = WESessionTrackService.getRegion(player);

        var cx = (int) region.asAABB().getCenter().x();
        var cy = (int) region.asAABB().getCenter().y();
        var cz = (int) region.asAABB().getCenter().z();

        var wrapped = event.getMask();
        var wrapper = new RadiusLimitedExtent(wrapped, cx, cy, cz, limit, cancel);

        wrapper.addAnnounce(() -> this.getLanguage().sendMessage(player, cancel ? "edit-limited" : "edit-limited-warn", limit));

        event.setMask(wrapper);
    }

    public static final class RadiusLimitedExtent implements Extent {
        private final List<Consumer<Vector3i>> callbacks = new ArrayList<>();
        private final List<Runnable> announces = new ArrayList<>();

        private final Extent wrapped;
        private final int centerX;
        private final int centerY;
        private final int centerZ;
        private final int radius;
        private final boolean cancelAction;

        private boolean isAnnounced = false;

        public RadiusLimitedExtent(Extent wrapped, int centerX, int centerY, int centerZ, int radius, boolean cancelAction) {
            this.wrapped = wrapped;
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
            this.radius = radius;
            this.cancelAction = cancelAction;
        }

        @SafeVarargs
        public final void addCallback(Consumer<Vector3i>... callbacks) {
            this.callbacks.addAll(List.of(callbacks));
        }

        public void addAnnounce(Runnable... announces) {
            this.announces.addAll(List.of(announces));
        }

        private boolean isInvalidPosition(int x, int y, int z) {
            var x0 = this.centerX - this.radius;
            var y0 = this.centerY - this.radius;
            var z0 = this.centerZ - this.radius;
            var x1 = this.centerX + this.radius;
            var y1 = this.centerY + this.radius;
            var z1 = this.centerZ + this.radius;

            return x < x0 || x > x1 || y < y0 || y > y1 || z < z0 || z > z1;
        }

        private void sendInvalid(int x, int y, int z) {
            var p = new Vector3i(x, y, z);

            for (var cb : this.callbacks) {
                cb.accept(p);
            }

            if (this.isAnnounced) {
                return;
            }
            for (var an : this.announces) {
                an.run();
            }
            this.isAnnounced = true;
        }

        @Override
        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
            var x = position.getBlockX();
            var y = position.getBlockY();
            var z = position.getBlockZ();

            if (isInvalidPosition(x, y, z)) {
                sendInvalid(x, y, z);

                if (this.cancelAction) {
                    return false;
                }
            }

            return this.wrapped.setBlock(position, block);
        }


        @Nullable
        @Override
        public Entity createEntity(Location location, BaseEntity entity) {
            var x = location.getBlockX();
            var y = location.getBlockY();
            var z = location.getBlockZ();

            if (isInvalidPosition(x, y, z)) {
                sendInvalid(x, y, z);

                if (this.cancelAction) {
                    return null;
                }
            }

            return this.wrapped.createEntity(location, entity);
        }

        @Nullable
        @Override
        public Operation commit() {
            return this.wrapped.commit();
        }

        @Override
        public BlockState getBlock(BlockVector3 position) {
            var x = position.getBlockX();
            var y = position.getBlockY();
            var z = position.getBlockZ();

            if (isInvalidPosition(x, y, z)) {
                assert BlockTypes.AIR != null;
                return BlockTypes.AIR.getDefaultState();
            }

            return this.wrapped.getBlock(position);
        }

        @Override
        public BaseBlock getFullBlock(BlockVector3 position) {
            return getBlock(position).toBaseBlock();
        }

        @Override
        public BlockVector3 getMinimumPoint() {
            return BlockVector3.ZERO;
        }

        @Override
        public BlockVector3 getMaximumPoint() {
            return BlockVector3.ZERO;
        }

        @Override
        public List<? extends Entity> getEntities(Region region) {
            return this.wrapped.getEntities(region);
        }

        @Override
        public List<? extends Entity> getEntities() {
            return this.wrapped.getEntities();
        }
    }
}
