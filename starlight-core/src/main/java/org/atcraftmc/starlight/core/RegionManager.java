package org.atcraftmc.starlight.core;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.atcraftmc.starlight.foundation.RegionDataManager;
import org.bukkit.Location;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

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

    public List<I> getIntersected(Location location) {
        List<I> lists = new ArrayList<>();
        for (I r : this.regions.values()) {
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

    public I getMinIntersected(Location location) {
        List<I> lists = getIntersected(location);
        if (lists.isEmpty()) {
            return null;
        }
        return lists.get(0);
    }

    public List<I> filter(Function<I, Boolean> filter) {
        List<I> lists = new ArrayList<>();
        for (I r : this.regions.values()) {
            if (filter.apply(r)) {
                lists.add(r);
            }
        }
        return lists;
    }
}
