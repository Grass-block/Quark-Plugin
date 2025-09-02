package org.atcraftmc.starlight.display;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleRO0;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.api.CustomChatRenderer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.foundation.TextExaminer;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.component.Components;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;
import org.atcraftmc.starlight.util.TemplateExpansion;

import java.util.Date;

@AutoRegister(ServiceType.EVENT_LISTEN)
@SLModule(version = "1.2.0")
@Components(ChatFormat.PaperChatListener.class)
public final class ChatFormat extends PackageModule {
    MethodHandleRO0<World, String> getDimensionId = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> World.class.getMethod("getKey"), (w) -> {
            var origin = w.getKey().toString();
            return origin.replace(":", "-").replace("_", "-").replace(".","-");
        });
        ctx.dummy((w) -> "minecraft-" + w.getName()
                .replace("world", "overworld")
                .replace("world-nether", "the-nether")
                .replace("world-the-end", "the-end")
                .replace("DIM0", "overworld")
                .replace("DIM1", "the-end")
                .replace("DIM-1", "the-nether"));
    });


    @EventHandler(priority = EventPriority.HIGH)
    public void onLegacyPlayerChat(AsyncPlayerChatEvent event) {
        var timeLine = this.getTime().replace("<post>", "");

        var expanded = TemplateExpansion.build((b) -> {
            b.replacement("time");
            b.replacement("0");
            b.replacement("1");
        }).expand(getTemplate(event.getPlayer()), timeLine, event.getPlayer().getDisplayName(), "%2$s");
        //fix vanilla chat-format issue

        event.setFormat(ComponentSerializer.legacy(TextBuilder.buildComponent(expanded)));
    }


    public String getTemplate(Player player) {
        if (this.getConfig().value("template").string() == null) {
            return "<{0}> {1}";
        }

        var wid = this.getDimensionId.invoke(player.getWorld()).replace("_", "-");
        var world = TextExaminer.examinableText(this.getConfig().value("world").string().formatted(wid));
        var template = this.getConfig().value("template").string();

        return PlaceHolderService.formatPlayer(player, template.replace("{world}", world));
    }

    public String getTime() {
        return this.getConfig().value("time").string().formatted(SharedObjects.TIME_FORMAT.format(new Date()));
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

