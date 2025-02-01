package org.atcraftmc.quark_velocity;

import me.gb2022.apm.remote.event.RemoteEventListener;
import me.gb2022.commons.reflect.AutoRegisterManager;

public final class ProxyModuleRegManager extends AutoRegisterManager<ProxyModule> {
    private final QuarkVelocity plugin;

    public ProxyModuleRegManager(QuarkVelocity plugin) {
        this.plugin = plugin;
    }

    public void deferredInit() {
        var events = this.plugin.getServer().getEventManager();

        registerHandler(Registers.VELOCITY_EVENT, (m) -> events.register(this.plugin, m), (m) -> events.unregisterListener(this.plugin, m));
        registerHandler(
                Registers.REMOTE_MESSAGE,
                (m) -> m.getMessenger().registerEventHandler(m),
                (m) -> m.getMessenger().removeMessageHandler(m)
        );
        registerHandler(
                Registers.APM_LISTENER,
                (m) -> m.getMessenger().eventChannel().addListener((RemoteEventListener) m),
                (m) -> m.getMessenger().eventChannel().removeListener((RemoteEventListener) m)
        );
    }
}
