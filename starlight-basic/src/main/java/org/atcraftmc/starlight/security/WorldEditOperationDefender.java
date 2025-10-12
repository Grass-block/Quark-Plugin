package org.atcraftmc.starlight.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.api.WEAction;
import org.atcraftmc.starlight.api.WESessionEditEvent;
import org.atcraftmc.starlight.api.WESessionPreEditEvent;
import org.atcraftmc.starlight.api.WESessionSelectEvent;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@SuppressWarnings("DuplicatedCode")
@ApplicationModule(id = "we-operation-defender", version = "1.3")
@AutoRegister(Registrations.SERVER_EVENT)
@CommandProvider(WorldEditOperationDefender.ConfirmCommand.class)
public final class WorldEditOperationDefender extends SLPackageModule {
    @Inject("-starlight.worldedit.size")
    public Permission bypassCheck;
    private Cache<UUID, String> commandCache;
    @Inject
    private LanguageEntry language;

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requirePlugin("WorldEdit");
    }

    @Override
    public void enable() throws Exception {
        var d = Duration.ofSeconds(this.config().value("confirm-time").intValue(15));
        this.commandCache = CacheBuilder.newBuilder().expireAfterWrite(d).build();
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        var m = event.getMessage();

        if (!m.startsWith("//") || m.startsWith("//pos") || m.startsWith("//confirm")) {
            return;
        }

        if (m.startsWith("//stack")) {
            var b = m.split(" ");
            if (b.length < 2) {
                return;
            }

            var len = Integer.parseInt(b[1]);
            if (len > this.config().value("warn-stack-length").intValue(50)) {
                submitForCheck(event);
                return;
            }

            var region = WESessionTrackService.getRegion(event.getPlayer());

            if (region == null) {
                return;
            }

            var box = region.asAABB();
            var limit = ConfigAccessor.getInt(this.config(), "warn-stack-effect-size");

            var x = box.x1 - box.x0;
            var y = box.y1 - box.y0;
            var z = box.z1 - box.z0;

            if (x * len <= limit && y * len <= limit && z * len <= limit) {
                return;
            }

            submitForCheck(event);
            return;
        }

        var region = WESessionTrackService.getRegion(event.getPlayer());

        if (region == null) {
            return;
        }

        var box = region.asAABB();

        var x = box.x1 - box.x0;
        var y = box.y1 - box.y0;
        var z = box.z1 - box.z0;

        var limit = ConfigAccessor.getInt(this.config(), "warn-selection-size");

        if (x <= limit && y <= limit && z <= limit) {
            return;
        }

        submitForCheck(event);
    }

    private void submitForCheck(PlayerCommandPreprocessEvent event) {
        this.language.item("confirm-required").send(event.getPlayer(), this.config().value("confirm-time").intValue(15));
        this.commandCache.put(event.getPlayer().getUniqueId(), event.getMessage().substring(1));
        event.setCancelled(true);
    }


    @EventHandler
    public void onSelect(WESessionPreEditEvent event) {
        var limit = ConfigAccessor.getInt(this.config(), "max-selection-size");
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
                MessageAccessor.send(this.language, player, "select-limited-warn", x, y, z, limit);
            }
            return;
        }

        if (event.getStage() == EditSession.Stage.BEFORE_CHANGE) {
            MessageAccessor.send(this.language, player, "select-limited", x, y, z, limit);
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onSelect(WESessionSelectEvent event) {
        if (event.getRegion() == null) {
            return;
        }
        var box = event.getRegion().asAABB();
        var limit = ConfigAccessor.getInt(this.config(), "max-selection-size");

        var x = box.x1 - box.x0;
        var y = box.y1 - box.y0;
        var z = box.z1 - box.z0;

        if (x <= limit && y <= limit && z <= limit) {
            return;
        }

        MessageAccessor.send(this.language, event.getPlayer(), "select-limited-warn", x, y, z, limit);
    }

    @EventHandler
    public void onEdit(WESessionEditEvent event) {
        if (WESessionTrackService.getLatestAction(event.getPlayer()) != WEAction.STACK) {
            return;
        }

        var player = event.getPlayer();
        var cancel = !player.hasPermission(this.bypassCheck);
        var limit = ConfigAccessor.getInt(this.config(), "max-edit-size");

        var region = WESessionTrackService.getRegion(player);

        var aabb = region.asAABB();

        var cx = (int) aabb.getCenter().x();
        var cy = (int) aabb.getCenter().y();
        var cz = (int) aabb.getCenter().z();

        var w = aabb.x1 - aabb.x0;
        var h = aabb.y1 - aabb.y0;
        var d = aabb.z1 - aabb.z0;

        var wrapped = event.getMask();
        var wrapper = new RadiusLimitedExtent(wrapped, cx, cy, cz, limit, cancel);

        wrapper.addAnnounce(() -> MessageAccessor.send(this.language, player, cancel ? "edit-limited" : "edit-limited-warn", limit));

        event.setMask(wrapper);
    }

    @QuarkCommand(name = "/confirm", permission = "+starlight.worldedit.confirm")
    public static final class ConfirmCommand extends ModuleCommand<WorldEditOperationDefender> {
        @Override
        public void execute(CommandExecution context) {
            var player = context.requireSenderAsPlayer();

            try {
                var cached = this.getModule().commandCache.get(player.getUniqueId(), () -> "");

                if (cached.isEmpty()) {
                    getLanguage().item("action-not-found").send(player);
                    return;
                }

                this.getModule().commandCache.invalidate(player.getUniqueId());

                getLanguage().item("action-confirmed").send(player, cached);
                player.performCommand(cached);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
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
