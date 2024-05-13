package org.tbstcraft.quark.service.base.task;

import org.bukkit.plugin.Plugin;

public interface Task {
    Plugin getOwner();

    void cancel();

    boolean isCancelled();
}
