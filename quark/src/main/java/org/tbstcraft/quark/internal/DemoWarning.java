package org.tbstcraft.quark.internal;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import me.gb2022.commons.reflect.AutoRegister;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.util.text.TextBuilder;
import org.tbstcraft.quark.util.text.TextSender;

@QuarkModule(id = "demo_warning", internal = true, available = FeatureAvailability.DEMO_ONLY)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class DemoWarning extends PackageModule {
    private static final String WARN_OP = """
            {#red} 您（管理）的服务器正在使用未授权的 {#purple}Quark{#red} 实例。请注意盗版软件可能的风险。
            """;

    private static final String WARN_PLAYER = """
            {#red} 您正在游玩的服务器使用了未授权的 {#purple}Quark{#red} 实例。请注意可能的数据安全风险。
            """;


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().isOp()) {
            TextSender.sendTo(event.getPlayer(), TextBuilder.build(WARN_OP));
        } else {
            TextSender.sendTo(event.getPlayer(), TextBuilder.build(WARN_PLAYER));
        }
    }
}
