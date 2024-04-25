package org.tbstcraft.quark.framework.permission;

import org.bukkit.permissions.PermissionAttachment;

public enum PermissionValue {
    TRUE,
    FALSE,
    UNSET;

    public static PermissionValue parse(String s) {
        return switch (s) {
            case "true" -> TRUE;
            case "false" -> FALSE;
            case "unset" -> UNSET;
            default -> throw new IllegalStateException("what the fuck is this: " + s);
        };
    }

    public static PermissionValue parse(boolean b) {
        return b ? TRUE : FALSE;
    }

    public void set(PermissionAttachment attachment, String path) {
        switch (this) {
            case TRUE -> attachment.setPermission(path, true);
            case FALSE -> attachment.setPermission(path, false);
            case UNSET -> attachment.unsetPermission(path);
        }
    }
}
