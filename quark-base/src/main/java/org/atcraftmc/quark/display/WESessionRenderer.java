package org.atcraftmc.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.quark.security.WESessionTrackService;
import org.atcraftmc.quark.security.event.WESessionSelectEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.foundation.region.SimpleRegion;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@QuarkModule(version = "1.0.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(WESessionRenderer.WESessionRenderCommand.class)
public final class WESessionRenderer extends PackageModule {
    private final Map<Player, RenderMode> modes = new HashMap<>();

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requirePlugin("WorldEdit");
    }

    @Override
    public void enable() {
        TaskService.global().timer("quark:we-renderer:main", 0, 5, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (mode(p) != RenderMode.PERSISTENT) {
                    continue;
                }

                draw(p);
            }
        });

        for (Player p : Bukkit.getOnlinePlayers()) {
            this.modes.put(p, load(p));
        }
    }

    @Override
    public void disable() {
        TaskService.global().cancel("quark:we-renderer:main");
    }

    @EventHandler
    public void onSelectionUpdate(WESessionSelectEvent event) {
        if (mode(event.getPlayer()) != RenderMode.UPDATE) {
            return;
        }

        render(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.modes.put(event.getPlayer(), load(event.getPlayer()));
    }

    private RenderMode mode(Player player) {
        return this.modes.computeIfAbsent(player, (k) -> RenderMode.UPDATE);
    }

    private void draw(Player p) {
        SimpleRegion r = WESessionTrackService.getRegion(p);
        if (r == null) {
            return;
        }
        Players.show3DBox(p, r.getPoint0(), r.getPoint1());
    }

    private void render(Player p) {
        AtomicInteger t = new AtomicInteger();

        TaskService.global().timer(0, 5, (ctx) -> {
            t.addAndGet(5);

            if (t.get() > 25) {
                ctx.cancel();
            }

            draw(p);
        });
    }

    private void save(Player player) {
        var entry = PlayerDataService.get(player);
        entry.setEnum("we_session:render_mode", this.modes.get(player));
        entry.save();
    }

    private RenderMode load(Player player) {
        var entry = PlayerDataService.get(player);
        if (!entry.hasKey("we_session:render_mode")) {
            return RenderMode.UPDATE;
        }
        return entry.getEnum("we_session:render_mode", RenderMode.class);
    }


    enum RenderMode {
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
                getLanguage().sendMessage(context.getSender(), "render");
                this.getModule().render(context.requireSenderAsPlayer());
                return;
            }

            var action = context.requireEnum(0, "off", "render", "update", "persistent");

            if (Objects.equals(action, "render")) {
                getLanguage().sendMessage(context.getSender(), "render");
                this.getModule().render(context.requireSenderAsPlayer());
                return;
            }

            getLanguage().sendMessage(context.getSender(), "mode-" + action);

            this.getModule().modes.put(context.requireSenderAsPlayer(), RenderMode.of(action));
            this.getModule().save(context.requireSenderAsPlayer());
        }
    }
}
