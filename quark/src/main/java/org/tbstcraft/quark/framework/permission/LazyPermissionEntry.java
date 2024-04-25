package org.tbstcraft.quark.framework.permission;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.command.CommandManager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class LazyPermissionEntry implements PermissionEntry {
    private final Permissible target;
    private final PermissionAttachment attachment;
    private final HashMap<String, Boolean> attachmentMap;

    @SuppressWarnings("unchecked")
    LazyPermissionEntry(Permissible target) {
        this.target = target;
        this.attachment = target.addAttachment(Quark.PLUGIN);

        try {
            Field f = this.attachment.getClass().getDeclaredField("permissions");
            f.setAccessible(true);
            this.attachmentMap = (HashMap<String, Boolean>) f.get(this.attachment);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void _setPermission(String path, PermissionValue value) {
        switch (value) {
            case TRUE -> this.attachmentMap.put(path, true);
            case FALSE -> this.attachmentMap.put(path, false);
            case UNSET -> this.attachmentMap.remove(path);
        }
    }

    private Set<String> dispatchPermission(String path) {
        if (!path.endsWith("*")) {
            return Set.of(path);
        } else {

            String namespace = path.replace(".*", "");
            Set<String> result = new HashSet<>();

            CommandMap map = CommandManager.getCommandMap();
            for (String cmd : CommandManager.getCommands()) {
                Command cmdObj = map.getCommand(cmd);
                if (cmdObj == null) {
                    continue;
                }
                String _perm = cmdObj.getPermission();
                if (_perm == null) {
                    continue;
                }
                for (String perm : _perm.split(";")) {
                    if (!perm.startsWith(namespace)) {
                        continue;
                    }
                    result.add(perm);
                }
            }
            result.add(namespace);
            return result;
        }
    }

    @Override
    public void setPermission(String path, PermissionValue value) {
        Set<String> perms = this.dispatchPermission(path);
        for (String s : perms) {
            this._setPermission(s, value);
        }
    }

    @Override
    public PermissionAttachment getAttachment() {
        return this.attachment;
    }

    @Override
    public Permissible getTarget() {
        return this.target;
    }

    @Override
    public void refresh() {
        this.getTarget().recalculatePermissions();
    }

    @Override
    public void clear() {
        this.attachmentMap.clear();
    }
}
