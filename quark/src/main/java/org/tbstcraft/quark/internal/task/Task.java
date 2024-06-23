package org.tbstcraft.quark.internal.task;

import org.bukkit.plugin.Plugin;

public interface Task {
    Plugin getOwner();

    void cancel();

    boolean isCancelled();
}
