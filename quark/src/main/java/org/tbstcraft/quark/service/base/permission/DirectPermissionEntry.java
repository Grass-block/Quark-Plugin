package org.tbstcraft.quark.service.base.permission;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import org.tbstcraft.quark.Quark;

public final class DirectPermissionEntry implements PermissionEntry {
    private final Permissible target;
    private final PermissionAttachment attachment;

    DirectPermissionEntry(Permissible target) {
        this.target = target;
        this.attachment = target.addAttachment(Quark.PLUGIN);
    }

    @Override
    public void setPermission(String path, PermissionValue value) {
        value.set(this.attachment, path);
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
    public void clear() {
        for (String s : this.attachment.getPermissions().keySet()) {
            this.attachment.unsetPermission(s);
        }
    }

    @Override
    public void refresh() {
        this.target.recalculatePermissions();
    }
}
