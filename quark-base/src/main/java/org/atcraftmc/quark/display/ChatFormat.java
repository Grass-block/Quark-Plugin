package org.atcraftmc.quark.display;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.reflect.AutoRegister;
import net.kyori.adventure.text.Component;
import org.atcraftmc.quark.CustomChatRenderer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.foundation.text.TextExaminer;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.foundation.text.ComponentSerializer;
import org.tbstcraft.quark.foundation.text.TextBuilder;
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
        var template = getTemplate(event.getPlayer());
        var time = this.getTime().replace("<post>", "");

        Component c = TextBuilder.buildComponent(template.replace("{time}", time), Component.text("%1$s"), Component.text("%2$s"));
        event.setFormat(ComponentSerializer.legacy(c));
    }


    public String getTemplate(Player player) {
        if (this.getConfig().getString("template") == null) {
            return "<{0}> {1}";
        }

        var wid=player.getWorld().getName().replace("_","-");
        var world = TextExaminer.examinableText(this.getConfig().getString("world").formatted(wid));
        var template = this.getConfig().getString("template");

        return template.replace("{world}", world);
    }

    public String getTime() {
        return this.getConfig().getString("time").formatted(SharedObjects.TIME_FORMAT.format(new Date()));
    }


    @AutoRegister(ServiceType.EVENT_LISTEN)
    public static final class PaperChatListener extends ModuleComponent<ChatFormat> {
        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("io.papermc.paper.event.player.AsyncChatEvent"));
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onChat(AsyncChatEvent event) {
            var template = this.parent.getTemplate(event.getPlayer());
            var time = this.parent.getTime();

            var render = CustomChatRenderer.renderer(event);

            if (time.startsWith("<post>")) {
                render.postfix(TextBuilder.buildComponent(time.substring(6)));
                template = template.replace("{time}", "");
            } else {
                template = template.replace("{time}", time);
            }

            render.template(template);
        }
    }
}

