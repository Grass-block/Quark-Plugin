package org.tbstcraft.quark.internal;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.event.EventHandler;
import org.tbstcraft.quark.api.DelayedPlayerJoinEvent;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.foundation.text.TextSender;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

@QuarkModule(id = "demo_warning", internal = true, available = FeatureAvailability.DEMO_ONLY)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class DemoWarning extends PackageModule {
    private static final String WARN_OP = """
            {#red} 您管理的服务器正在使用未经验证的 {#purple}Quark{#red} 插件实例。
            {#red} 请注意盗版软件可能导致的后门和数据泄露等风险。
            {#red} 我们不对您当前所使用的的该版本负责。
            """;

    private static final String WARN_PLAYER = """
            {#red} 您正在游玩的服务器使用了未授权的 {#purple}Quark{#red} 插件实例。
            {#red} 请注意可能的隐私泄露和数据安全风险。
            {#red} 我们不对该服务器当前所使用的的该版本负责。
            """;


    @EventHandler
    public void onPlayerJoin(DelayedPlayerJoinEvent event) {
        if (event.getPlayer().isOp()) {
            TextSender.sendTo(event.getPlayer(), TextBuilder.build(WARN_OP));
        } else {
            TextSender.sendTo(event.getPlayer(), TextBuilder.build(WARN_PLAYER));
        }
    }
}
