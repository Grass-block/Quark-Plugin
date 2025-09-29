package org.atcraftmc.starlight.display;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleRO0;
import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import me.gb2022.modular.subcomponent.ComponentProvider;
import org.atcraftmc.qlib.config.ConfigContainer;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.api.CustomChatRenderer;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.foundation.TextExaminer;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.SLModuleComponent;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.util.TemplateExpansion;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Date;

@AutoRegister(Registrations.SERVER_EVENT)
@ApplicationModule(id = "chat-format", version = "1.2.0")
@ComponentProvider(ChatFormat.PaperChatListener.class)
public final class ChatFormat extends SLPackageModule {
    MethodHandleRO0<World, String> getDimensionId = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> World.class.getMethod("getKey"), (w) -> {
            var origin = w.getKey().toString();
            return origin.replace(":", "-").replace("_", "-").replace(".", "-");
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
        if (this.config().value("template").string() == null) {
            return "<{0}> {1}";
        }

        var wid = this.getDimensionId.invoke(player.getWorld()).replace("_", "-");
        var world = TextExaminer.examinableText(this.config().value("world").string().formatted(wid));
        var template = this.config().value("template").string();

        return PlaceHolderService.formatPlayer(player, template.replace("{world}", world));
    }

    public String getTime() {
        return this.config().value("time").string().formatted(SharedObjects.TIME_FORMAT.format(new Date()));
    }


    @AutoRegister(Registrations.SERVER_EVENT)
    public static final class PaperChatListener extends SLModuleComponent<ChatFormat> {
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

