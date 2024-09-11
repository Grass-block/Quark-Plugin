package org.tbstcraft.quark.internal.task.bukkit;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.internal.task.Task;

public final class BukkitTaskWrapper implements Task {
    private BukkitTask handle;

    public void setHandle(BukkitTask handle) {
        this.handle = handle;
    }

    @Override
    public Plugin getOwner() {
        if (this.handle == null) {
            return Quark.getInstance();
        }
        return this.handle.getOwner();
    }

    @Override
    public void cancel() {
        if (this.handle == null) {
            return;
        }
        this.handle.cancel();
    }

    @Override
    public boolean isCancelled() {
        if (this.handle == null) {
            return false;
        }
        return this.handle.isCancelled();
    }
}
