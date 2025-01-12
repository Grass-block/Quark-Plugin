package org.atcraftmc.quark_velocity;

import com.velocitypowered.api.event.EventManager;
import me.gb2022.commons.reflect.AutoRegisterManager;

public final class ProxyModuleRegManager extends AutoRegisterManager<ProxyModule> {
    private final QuarkVelocity plugin;

    public ProxyModuleRegManager(QuarkVelocity plugin) {
        this.plugin = plugin;
    }

    public void deferredInit() {
        EventManager events = this.plugin.getServer().getEventManager();

        registerHandler(Registers.VELOCITY_EVENT, (m) -> events.register(this.plugin, m), (m) -> events.unregisterListener(this.plugin, m));
        registerHandler(Registers.REMOTE_MESSAGE, (m) -> m.getMessenger().addMessageHandler(m), (m) -> m.getMessenger().removeMessageHandler(m));
    }
}
