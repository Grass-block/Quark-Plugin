package org.atcraftmc.quark.display;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.reflect.AutoRegister;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.atcraftmc.quark.CustomChatRenderer;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.component.Components;
import org.tbstcraft.quark.framework.module.component.ModuleComponent;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.Date;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.2.0")
@Components(ChatFormat.PaperChatListener.class)
public final class ChatFormat extends PackageModule {

    @EventHandler(priority = EventPriority.HIGH)
    public void onLegacyPlayerChat(AsyncPlayerChatEvent event) {
        String template = this.getConfig().getString("template");
        if (template == null) {
            return;
        }

        Component c = TextBuilder.buildComponent(template, Component.text("%1$s"), Component.text("%2$s"));
        event.setFormat(LegacyComponentSerializer.legacySection().serialize(c));
    }

    @AutoRegister(ServiceType.EVENT_LISTEN)
    public static final class PaperChatListener extends ModuleComponent<ChatFormat> {
        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("io.papermc.paper.event.player.AsyncChatEvent"));
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onChat(AsyncChatEvent event) {
            if (this.getConfig().getString("template") == null) {
                return;
            }

            String template = this.parent.getConfig().getString("template");
            String time = this.parent.getConfig().getString("time")
                    .formatted(SharedObjects.TIME_FORMAT.format(new Date()));
            CustomChatRenderer.renderer(event).template(template).postfix(TextBuilder.buildComponent(time));
        }
    }
}

