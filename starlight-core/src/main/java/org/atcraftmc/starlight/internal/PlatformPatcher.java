package org.atcraftmc.starlight.internal;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.component.Components;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.Registers;
import org.atcraftmc.starlight.core.TaskService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SLModule(description = "Provide fixes for certain platform.")
@Components({PlatformPatcher.PlayerLastLoginPatch.class, PlatformPatcher.ServerTPSPatch.class, PlatformPatcher.LegacyCommandTimingsPatch.class})
public final class PlatformPatcher extends PackageModule {
    static PlatformPatcher INSTANCE;

    public static Optional<PlatformPatcher> instance() {
        return Optional.ofNullable(INSTANCE);
    }

    @Override
    public void enable() {
        INSTANCE = this;
    }

    public PlayerLastLoginPatch lastLogin() {
        return getComponent(PlayerLastLoginPatch.class);
    }

    public ServerTPSPatch tps() {
        return getComponent(ServerTPSPatch.class);
    }


    @AutoRegister(Registers.BUKKIT_EVENT)
    public static final class PlayerLastLoginPatch extends ModuleComponent<PlatformPatcher> {
        private final Map<String, Long> cache = new HashMap<>();

        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.reversed("supported", () -> Compatibility.requireMethod(() -> Player.class.getMethod("getLastLogin")));
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            this.cache.put(PlayerIdentificationService.transformPlayer(event.getPlayer()), System.currentTimeMillis());
        }


        public long get(Player player) {
            return this.cache.computeIfAbsent(PlayerIdentificationService.transformPlayer(player), p -> System.currentTimeMillis());
        }
    }

    public static final class ServerTPSPatch extends ModuleComponent<PlatformPatcher> {
        private long lastTick;
        private double tps;

        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.reversed("supported", () -> Compatibility.requireMethod(() -> Server.class.getMethod("getTPS")));
            Compatibility.reversed("supported", () -> Compatibility.requireMethod(() -> Bukkit.class.getMethod("getTPS")));
        }

        @Override
        public void enable() {
            TaskService.global().timer("quark:tps:timer", 1, 1, () -> {
                var now = System.currentTimeMillis();
                var mspt = (int) (now - this.lastTick);

                this.tps = 1000f / mspt;

                this.lastTick = System.currentTimeMillis();
            });
        }

        @Override
        public void disable() {
            TaskService.global().cancel("quark:tps:timer");
        }

        public double get() {
            return this.tps;
        }
    }

    @SuppressWarnings("removal")//legacy compat
    @AutoRegister(Registers.BUKKIT_EVENT)
    public static final class LegacyCommandTimingsPatch extends ModuleComponent<PlatformPatcher> {

        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.assertion(Bukkit.getServer().getVersion().contains("PaperSpigot"));
            Compatibility.requireClass(() -> Class.forName("co.aikar.timings.TimingsManager"));
        }

        @Override
        public void enable() {
            this.inject();
        }

        @EventHandler
        public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
            this.inject();
        }

        @EventHandler
        public void onServerCommand(ServerCommandEvent event) {
            this.inject();
        }

        public void inject() {
            for (var c : LegacyCommandManager.getKnownCommands(LegacyCommandManager.getCommandMap()).values()) {
                if (c.timings == null) {
                    c.timings = co.aikar.timings.TimingsManager.getCommandTiming("_quark_inject", c);
                }
            }
        }
    }
}
