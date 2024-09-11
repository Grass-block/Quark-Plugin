package org.tbstcraft.quark.internal.task.folia;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.internal.task.Task;

public final class ThreadedRegionTaskWrapper implements Task {
    private ScheduledTask handle;

    public void setHandle(ScheduledTask handle) {
        this.handle = handle;
    }

    @Override
    public Plugin getOwner() {
        if (this.handle == null) {
            return Quark.getInstance();
        }
        return this.handle.getOwningPlugin();
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
            return true;
        }
        return this.handle.isCancelled();
    }
}
