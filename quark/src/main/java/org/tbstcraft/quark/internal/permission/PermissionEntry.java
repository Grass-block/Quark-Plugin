package org.tbstcraft.quark.internal.permission;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.tbstcraft.quark.foundation.command.CommandManager;
import org.tbstcraft.quark.foundation.command.QuarkCommand;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface PermissionEntry {
    static Set<String> getAllPermissions() {
        Set<String> set = new HashSet<>();

        for (Permission p : Bukkit.getPluginManager().getPermissions()) {
            set.addAll(List.of(p.getName().split(";")));
        }
        for (Command c : CommandManager.getCommandEntries().values()) {
            if (c.getPermission() == null || c.getPermission().isEmpty()) {
                continue;
            }
            set.addAll(List.of(c.getPermission().split(";")));
        }

        set.remove(QuarkCommand.NO_INFO);

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
