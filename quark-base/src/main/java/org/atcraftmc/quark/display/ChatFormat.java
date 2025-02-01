package org.atcraftmc.quark.display;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleRO0;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.quark.CustomChatRenderer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.foundation.ComponentSerializer;
import org.tbstcraft.quark.foundation.TextExaminer;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.component.Components;
import org.tbstcraft.quark.framework.module.component.ModuleComponent;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
import org.tbstcraft.quark.util.TemplateExpansion;

import java.util.Date;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.2.0")
@Components(ChatFormat.PaperChatListener.class)
public final class ChatFormat extends PackageModule {
    MethodHandleRO0<World, String> getDimensionId = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> World.class.getMethod("key"), (w) -> w.getKey().toString());
        ctx.dummy((w) -> "minecraft:" + w.getName()
                .replace("DIM0", "world")
                .replace("DIM1", "world_the_end")
                .replace("DIM-1", "world_nether"));
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
        if (this.getConfig().getString("template") == null) {
            return "<{0}> {1}";
        }

        var wid = this.getDimensionId.invoke(player.getWorld()).replace("_", "-");
        var world = TextExaminer.examinableText(this.getConfig().getString("world").formatted(wid));
        var template = this.getConfig().getString("template");

        return PlaceHolderService.formatPlayer(player, template.replace("{world}", world));
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

