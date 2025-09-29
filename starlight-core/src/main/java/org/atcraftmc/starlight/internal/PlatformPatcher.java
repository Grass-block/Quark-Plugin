package org.atcraftmc.starlight.internal;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import me.gb2022.modular.subcomponent.ComponentProvider;
import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.SLModuleComponent;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationModule(id = "platform-patcher", description = "Provide fixes for certain platform.")
@ComponentProvider({PlatformPatcher.PlayerLastLoginPatch.class, PlatformPatcher.ServerTPSPatch.class, PlatformPatcher.LegacyCommandTimingsPatch.class})
public final class PlatformPatcher extends SLPackageModule {
    static PlatformPatcher INSTANCE;

    public static Optional<PlatformPatcher> instance() {
        return Optional.ofNullable(INSTANCE);
    }

    @Override
    public void enable() {
        INSTANCE = this;
    }

    public PlayerLastLoginPatch lastLogin() {
        return handle().getComponent(PlayerLastLoginPatch.class);
    }

    public ServerTPSPatch tps() {
        return handle().getComponent(ServerTPSPatch.class);
    }


    @AutoRegister(Registrations.SERVER_EVENT)
    public static final class PlayerLastLoginPatch extends SLModuleComponent<PlatformPatcher> {
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

    public static final class ServerTPSPatch extends SLModuleComponent<PlatformPatcher> {
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
    @AutoRegister(Registrations.SERVER_EVENT)
    public static final class LegacyCommandTimingsPatch extends SLModuleComponent<PlatformPatcher> {

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
