package org.tbstcraft.quark.framework.permission;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;

public interface PermissionEntry {
    boolean USE_DIRECT_PERMISSION_ENTRY = false;
    HashMap<Permissible, PermissionEntry> CACHE = new HashMap<>();

    static PermissionEntry get(Permissible target) {
        if (CACHE.containsKey(target)) {
            return CACHE.get(target);
        }
        PermissionEntry entry = USE_DIRECT_PERMISSION_ENTRY ? new DirectPermissionEntry(target) : new LazyPermissionEntry(target);
        CACHE.put(target, entry);
        return entry;
    }

    void setPermission(String path, PermissionValue value);

    default void setAndRefresh(String path, PermissionValue value) {
        setPermission(path, value);
        refresh();
    }

    PermissionAttachment getAttachment();

    Permissible getTarget();

    void clear();

    void refresh();

}
