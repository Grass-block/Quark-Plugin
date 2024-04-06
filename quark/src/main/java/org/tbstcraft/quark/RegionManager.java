package org.tbstcraft.quark;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.Location;
import org.tbstcraft.quark.service.data.ModuleDataService;
import org.tbstcraft.quark.util.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class RegionManager {
    private final String ownerId;
    private final String entryId;

    private final HashMap<String, Region> regions = new HashMap<>();

    public RegionManager(String ownerId, String entryId) {
        this.ownerId = ownerId;
        this.entryId = entryId;
    }

    public void loadRegions() {
        NBTTagCompound tag = ModuleDataService.getEntry(this.ownerId);
        this.regions.clear();
        for (String s : tag.getTagMap().keySet()) {
            this.regions.put(s, new Region(tag.getCompoundTag(s)));
        }
    }

    public void saveRegions() {
        NBTTagCompound tag = this.getSaveEntry();
        for (String s : this.regions.keySet()) {
            tag.setCompoundTag(s, this.regions.get(s).serialize());
        }
        ModuleDataService.save(this.ownerId);
    }

    private NBTTagCompound getSaveEntry() {
        NBTTagCompound tag = ModuleDataService.getEntry(this.ownerId);
        if (!tag.hasKey(this.entryId)) {
            NBTTagCompound entry = new NBTTagCompound();
            tag.setCompoundTag(this.entryId, entry);
            return entry;
        }
        return tag.getCompoundTag(this.entryId);
    }

    public void addRegion(String id, Region region) {
        this.regions.put(id, region);
    }

    public void removeRegion(String id) {
        this.regions.remove(id);
    }

    public List<Region> getIntersected(Location location) {
        List<Region> lists = new ArrayList<>();
        for (Region r : this.regions.values()) {
            if (r.contains(location)) {
                lists.add(r);
            }
        }
        lists.sort((o1, o2) -> {
            if (o1 == o2) {
                return 0;
            }
            return -Double.compare(o1.asAABB().getMaxWidth(), o2.asAABB().getMaxWidth());
        });
        return lists;
    }

    public Region getMinIntersected(Location location) {
        List<Region> lists = getIntersected(location);
        if (lists.isEmpty()) {
            return null;
        }
        return lists.get(0);
    }
}
