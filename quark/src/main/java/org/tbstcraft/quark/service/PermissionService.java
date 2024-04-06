package org.tbstcraft.quark.service;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.tbstcraft.quark.util.ObjectContainer;

import java.util.HashMap;
import java.util.Map;

@ServiceImplementation(impl = PermissionService.Impl.class)
public interface PermissionService extends Service {
    ObjectContainer<PermissionService> INSTANCE = new ObjectContainer<>();

    static Permission createObject(String perm) {
        String id = perm.substring(1);
        return new Permission(id, switch (perm.charAt(0)) {
            case '+' -> PermissionDefault.TRUE;
            case '-' -> PermissionDefault.OP;
            case '!' -> PermissionDefault.FALSE;
            default -> throw new RuntimeException("invalid value:" + perm.charAt(0));
        });
    }

    static void createPermission(String fmt) {
        INSTANCE.get().create(fmt);
    }

    static void deletePermission(String fmt) {
        INSTANCE.get().delete(fmt);
    }

    void create(String fmt);

    void delete(String fmt);

    final class Impl implements PermissionService {
        private final Map<String, Permission> map = new HashMap<>();

        @Override
        public void onDisable() {
            for (Permission m : this.map.values()) {
                Bukkit.getPluginManager().removePermission(m);
            }
        }

        @Override
        public void create(String perm) {
            Permission permission = createObject(perm);
            Permission replacement=Bukkit.getPluginManager().getPermission(perm.substring(1));
            if (replacement != null) {
                replacement.setDefault(permission.getDefault());
            }else {
                Bukkit.getPluginManager().addPermission(permission);
            }
            this.map.put(permission.getName(), permission);
        }

        @Override
        public void delete(String perm) {
            String id = perm.substring(1);
            Bukkit.getPluginManager().removePermission(id);
            this.map.remove(id);
        }
    }
}
