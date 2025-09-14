package org.atcraftmc.starlight.display;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.starlight.api.WESessionSelectEvent;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.data.FlexibleMapService;
import org.atcraftmc.starlight.core.data.flex.TableColumn;
import org.atcraftmc.starlight.core.SimpleRegion;
import org.atcraftmc.starlight.data.PlayerDataService;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.foundation.platform.Players;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.security.WESessionTrackService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@SLModule(version = "1.0.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(WESessionRenderer.WESessionRenderCommand.class)
public final class WESessionRenderer extends PackageModule implements FlexibleMapService.Codec<WESessionRenderer.RenderMode> {
    private final TableColumn<RenderMode> RENDER_MODE = TableColumn.custom("we_render_mode", 24, RenderMode.UPDATE, this);

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requirePlugin("WorldEdit");
    }

    @Override
    public void enable() {
        TaskService.global().timer("quark:we-renderer:main", 0, 5, () -> {
            for (var p : Bukkit.getOnlinePlayers()) {
                if (getMode(p) != RenderMode.PERSISTENT) {
                    continue;
                }

                draw(p);
            }
        });
    }

    @Override
    public void disable() {
        TaskService.global().cancel("quark:we-renderer:main");
    }

    @EventHandler
    public void onSelectionUpdate(WESessionSelectEvent event) {
        if (getMode(event.getPlayer()) != RenderMode.UPDATE) {
            return;
        }

        render(event.getPlayer());
    }

    private void draw(Player p) {
        SimpleRegion r = WESessionTrackService.getRegion(p);
        if (r == null) {
            return;
        }
        Players.show3DBox(p, r.getPoint0(), r.getPoint1());
    }

    private void render(Player p) {
        var t = new AtomicInteger();

        TaskService.global().timer(0, 5, (ctx) -> {
            t.addAndGet(5);

            if (t.get() > 25) {
                ctx.cancel();
            }

            draw(p);
        });
    }

    private RenderMode getMode(Player player) {
        return RENDER_MODE.get(PlayerDataService.PLAYER_SHARED, player.getUniqueId());
    }

    @Override
    public String encode(RenderMode data) {
        return data.name();
    }

    @Override
    public RenderMode decode(String data) {
        return RenderMode.valueOf(data);
    }

    private void setMode(Player player, RenderMode of) {
        RENDER_MODE.set(PlayerDataService.PLAYER_SHARED, player.getUniqueId(), of);
    }

    public enum RenderMode {
        NEVER, UPDATE, PERSISTENT;

        static RenderMode of(String id) {
            return switch (id) {
                case "update" -> RenderMode.UPDATE;
                case "persistent" -> RenderMode.PERSISTENT;
                default -> RenderMode.NEVER;
            };
        }
    }

    @QuarkCommand(name = "we-selection", aliases = {"render-we", "/render-sel", "/render-selection", "/render", "render-we-selection"})
    public static final class WESessionRenderCommand extends ModuleCommand<WESessionRenderer> {
        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, "off", "render", "update", "persistent");
        }

        @Override
        public void execute(CommandExecution context) {
            if (!context.hasArgumentAt(0)) {
                MessageAccessor.send(this.getLanguage(), context.getSender(), "render");
                this.getModule().render(context.requireSenderAsPlayer());
                return;
            }

            var action = context.requireEnum(0, "off", "render", "update", "persistent");

            if (Objects.equals(action, "render")) {
                MessageAccessor.send(this.getLanguage(), context.getSender(), "render");
                this.getModule().render(context.requireSenderAsPlayer());
                return;
            }

            MessageAccessor.send(this.getLanguage(), context.getSender(), "mode-" + action);

            this.getModule().setMode(context.requireSenderAsPlayer(), RenderMode.of(action));
        }
    }
}
