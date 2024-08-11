package org.atcraftmc.quark.velocity;

import org.atcraftmc.quark.velocity.features.HubCommand;
import org.atcraftmc.quark.velocity.features.MotdSync;
import org.atcraftmc.quark.velocity.peer.*;

public final class ModuleManager {
    public static final ProxyModule[] MODULES = new ProxyModule[]{
            new HubCommand(),
            new MotdSync(),
            new JoinQuitMessage(),
            new ChatSync(),
            new BungeeConnectionProtect(),
            new ProxyPing(),
            new ServerStatementObserver()
    };
    private final QuarkVelocity plugin;

    public ModuleManager(QuarkVelocity plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        for (ProxyModule module : MODULES) {
            module.context(this.plugin, this.plugin.getServer());
            this.plugin.getRegManager().attach(module);
            module.enable();
        }
    }

    public void disable() {
        for (ProxyModule module : MODULES) {
            this.plugin.getRegManager().detach(module);
            module.disable();
        }
    }
}
