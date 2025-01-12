package org.atcraftmc.quark.proxysupport;

import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.RemoteMessageService;

@QuarkModule(version = "1.3")
public final class ServerStatementObserver extends PackageModule {

    @Override
    public void enable() {
        RemoteMessageService.messenger().sendBroadcast("server:status", "online");
    }

    @Override
    public void disable() {
        RemoteMessageService.messenger().sendBroadcast("server:status", "offline");
    }
}
