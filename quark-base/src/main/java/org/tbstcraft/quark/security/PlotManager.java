package org.tbstcraft.quark.security;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.data.ModuleDataService;
import org.tbstcraft.quark.util.platform.PlayerUtil;
import org.tbstcraft.quark.util.region.Region;
import org.tbstcraft.quark.util.region.RegionDataManager;
import org.tbstcraft.quark.util.region.RegionManager;

import java.util.*;

@Deprecated(since = "//没做完，但是他们说不需要了")
@QuarkModule
@AutoRegister(ServiceType.EVENT_LISTEN)
public class PlotManager extends PackageModule {
    private final Map<String, PlotRegion> editModePlayers = new HashMap<>();
    private RegionManager<PlotRegion> regionManager;

    @Override
    public void enable() {
        this.regionManager = new RegionManager<>(PlotRegion.class, RegionDataManager.NBT(ModuleDataService.getEntry(this.getFullId())));
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.editModePlayers.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        PlotRegion region = this.regionManager.getMinIntersected(event.getPlayer().getLocation());

        if (this.editModePlayers.containsKey(player.getName())) {
            event.setCancelled(true);
            if (region != null) {
                getLanguage().sendMessage(event.getPlayer(), "edit-conflict");
                return;
            }

            PlotRegion edit = this.editModePlayers.get(player.getName());

            if (event.hasBlock()) {
                return;
            }
            if (event.getAction().isLeftClick()) {
                edit.setPoint0(Objects.requireNonNull(event.getClickedBlock()).getLocation());
                getLanguage().sendMessage(event.getPlayer(), "edit-p0");
            }
            if (event.getAction().isRightClick()) {
                edit.setPoint1(Objects.requireNonNull(event.getClickedBlock()).getLocation());
                getLanguage().sendMessage(event.getPlayer(), "edit-p1");
            }

            PlayerUtil.show3DBox(event.getPlayer(), edit.getPoint0(), edit.getPoint1());
            return;
        }


        if (region == null) {
            if (this.getConfig().getBoolean("allow-access-public-area")) {
                return;
            }
            event.setCancelled(true);
            getLanguage().sendMessage(event.getPlayer(), "deny-public-area");
            return;
        }
        if (region.canAccess(event.getPlayer().getName())) {
            return;
        }
        event.setCancelled(true);
        getLanguage().sendMessage(event.getPlayer(), "deny-inside-area", region.getOwner());
    }

    private void startEditing(Player player) {
        PlotRegion region = new PlotRegion(player.getName(), player.getWorld());
        this.editModePlayers.put(player.getName(), region);
    }

    private EditResult stopEditing(Player player) {
        PlotRegion region = this.editModePlayers.get(player.getName());
        if (region == null) {
            return EditResult.NOT_FOUND;
        }
        if (!region.isComplete()) {
            return EditResult.NOT_COMPLETE;
        }
        if (region.asAABB().getMaxWidth() > this.getConfig().getInt("own-count")) {
            return EditResult.TOO_BIG;
        }
        if (this.regionManager.filter((r) -> Objects.equals(r.getOwner(), player.getName())).size() > this.getConfig().getInt("own-count")) {
            return EditResult.CANNOT_ALLOCATE;
        }

        this.regionManager.addRegion(region.getOwner(), region);
        this.regionManager.saveRegions();
        ModuleDataService.save(this.getFullId());
        return EditResult.COMPLETE;
    }


    private enum EditResult {NOT_FOUND, TOO_BIG, NOT_COMPLETE, COMPLETE, CANNOT_ALLOCATE}

    private static class PlotRegion extends Region {
        Set<String> members = new HashSet<>();
        String owner;

        public PlotRegion(String owner, World world, int x0, int y0, int z0, int x1, int y1, int z1) {
            super(world, x0, y0, z0, x1, y1, z1);
            this.owner = owner;
        }

        public PlotRegion(String owner, World world) {
            super(world);
            this.owner = owner;
        }

        @Override
        public void readAdditionData(NBTTagCompound addition) {
            this.owner = addition.getString("owner");
        }

        @Override
        public void writeAdditionData(NBTTagCompound addition) {
            addition.setString("owner", this.owner);
        }

        public Set<String> getMembers() {
            return members;
        }

        public String getOwner() {
            return owner;
        }

        public boolean canAccess(String name) {
            return Objects.equals(this.owner, name) || this.members.contains(name);
        }
    }

    public static final class PlotCommand extends ModuleCommand<PlotManager> {

    }
}
