package org.atcraftmc.quark_velocity;

import org.atcraftmc.quark_velocity.features.*;

import java.util.HashMap;
import java.util.Map;

public final class ModuleManager {
    private final QuarkVelocity plugin;

    private final Map<String, ProxyModule> modules = new HashMap<>();

    public ModuleManager(QuarkVelocity plugin) {
        this.plugin = plugin;

        this.modules.put("stp-command", new STPCommand());
        this.modules.put("stpa-command", new STPACommand());
        this.modules.put("hub-command", new HUBCommand());
        this.modules.put("legacy-forwarding-protect", new LegacyForwardingProtect());
        this.modules.put("server-statement-observer", new ServerStatementObserver());
        this.modules.put("server-transfer-message", new ServerTransferMessage());
        this.modules.put("proxy-ping", new ProxyPing());
        this.modules.put("motd-sync", new MotdSync());
        this.modules.put("chat-sync", new ChatSync());
        this.modules.put("tab-sync", new TabSync());
        this.modules.put("mod-server-support", new ModdedServerSupport());
        this.modules.put("player-action-restriction", new PlayerActionRestriction());
    }

    public void enable() {
        for (var id : this.modules.keySet()) {
            var m = this.modules.get(id);

            if (Config.featureEnabled(id)) {
                m.context(this.plugin, this.plugin.getServer());
                this.plugin.getRegManager().attach(m);
                m.enable();
            }
        }
    }

    public void disable() {
        for (var id : this.modules.keySet()) {
            var m = this.modules.get(id);

            if (Config.featureEnabled(id)) {
                this.plugin.getRegManager().detach(m);
                m.disable();
            }
        }
    }
}
