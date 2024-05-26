package org.tbstcraft.quark.service.base.permission;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.tbstcraft.quark.framework.command.CommandManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    static Set<String> getAllPermissions(){
        Set<String> set = new HashSet<>();

        for (Permission p : Bukkit.getPluginManager().getPermissions()) {
            set.addAll(List.of(p.getName().split(";")));
        }
        for (Command c : CommandManager.getCommandEntries().values()) {
            if(c.getPermission()==null||c.getPermission().isEmpty()){
                continue;
            }
            set.addAll(List.of(c.getPermission().split(";")));
        }
        return set;
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
