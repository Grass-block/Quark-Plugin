package org.tbstcraft.quark.internal.permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceHolder;
import org.tbstcraft.quark.framework.service.ServiceInject;

import java.util.HashMap;
import java.util.Map;

@QuarkService(id = "permission", impl = PermissionService.Impl.class)
public interface PermissionService extends Service {

    @ServiceInject
    ServiceHolder<PermissionService> INSTANCE = new ServiceHolder<>();

    static Permission getPermission(String codec) {
        String name = codec;

        if (codec.matches("^[+\\-!].*")) {
            name = codec.substring(1);
        }

        Permission permission = Bukkit.getPluginManager().getPermission(name);

        if (permission == null) {
            if (codec.matches("^[+\\-!].*")) {
                Quark.LOGGER.warning("created unregistered permission " + codec);
                permission = createPermissionObject(codec);
            }
        }

        return permission;
    }

    static Permission createObject(String perm) {
        String id = perm.substring(1);
        if (!id.matches("^[a-z.]+$")) {
            throw new IllegalArgumentException("Invalid permission id: " + id);
        }

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

    static Permission createPermissionObject(String fmt) {
        return INSTANCE.get().create(fmt);
    }

    static void update() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.recalculatePermissions();
        }
    }

    static void deletePermission(String fmt) {
        INSTANCE.get().delete(fmt);
    }

    Permission create(String fmt);

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
        public Permission create(String perm) {
            Permission permission = createObject(perm);

            if (perm.matches("^[a-z.]+$")) {
                perm = perm.substring(1);
            }

            Permission replacement = Bukkit.getPluginManager().getPermission(perm);
            if (replacement != null) {
                replacement.setDefault(permission.getDefault());
            } else {
                try {
                    Bukkit.getPluginManager().addPermission(permission);
                }catch (Exception ignored){
                    //Quark.LOGGER.warning("duplicated permission:" + perm);
                }
            }

            this.map.put(permission.getName(), permission);
            return permission;
        }

        @Override
        public void delete(String perm) {
            String id = perm.substring(1);
            Bukkit.getPluginManager().removePermission(id);
            this.map.remove(id);
        }
    }
}
