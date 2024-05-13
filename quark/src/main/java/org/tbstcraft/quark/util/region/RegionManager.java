package org.tbstcraft.quark.util.region;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.Location;
import org.tbstcraft.quark.internal.data.ModuleDataService;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class RegionManager<I extends Region> {
    private final HashMap<String, I> regions = new HashMap<>();
    private final Class<I> template;
    private final RegionDataManager dataManager;

    public RegionManager(Class<I> template, RegionDataManager dataManager) {
        this.template = template;
        this.dataManager = dataManager;
    }


    public static <T> T deserializeRegion(NBTTagCompound tag, Class<T> type) {
        try {
            return type.getDeclaredConstructor(NBTTagCompound.class).newInstance(tag);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static NBTTagCompound serializeRegion(Region r) {
        NBTTagCompound tag = new NBTTagCompound();
        r.write(tag);
        return tag;
    }


    public void loadRegions() {
        this.regions.clear();
        for (String s : this.dataManager.list()) {
            this.regions.put(s, deserializeRegion(this.dataManager.load(s), this.template));
        }
    }

    public void saveRegions() {
        for (String s : this.regions.keySet()) {
            this.dataManager.save(s, this.regions.get(s).serialize());
        }
    }

    public void addRegion(String id, I region) {
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