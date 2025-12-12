package org.atcraftmc.quark.proxy;

import com.google.gson.JsonParser;
import me.gb2022.commons.http.HttpMethod;
import me.gb2022.commons.http.HttpRequest;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.Registrations;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.util.ExceptionUtil;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@ApplicationModule(defaultEnable = false, version = "1.2")
@AutoRegister({Registrations.SERVER_EVENT, Registrations.PLUGIN_VPN_EVENT})
@CommandProvider(MCSMDynamicInstance.JoinDynamicServerCommand.class)
public final class MCSMDynamicInstance extends SLPackageModule {
    @Inject
    private Logger logger;
    
    @Override
    public void enable() {
        if (!ConfigAccessor.getBool(this.config(), "instance")) {
            return;
        }
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            this.scheduleStop();
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (!ConfigAccessor.getBool(this.config(), "instance")) {
            return;
        }
        this.logger.info("cancel server stopping process as POWER_SAVING");
        TaskService.async().cancel("quark:ps:countdown");
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (!ConfigAccessor.getBool(this.config(), "instance")) {
            return;
        }
        if (Bukkit.getOnlinePlayers().size() == 1) {
            this.scheduleStop();
        }
    }

    public void scheduleStop() {
        this.logger.info("starting stopping countdown");
        TaskService.async().delay("quark:ps:countdown", ConfigAccessor.getInt(config(), "shutdown-delay"), () -> {
            this.logger.info("stopping server as POWER_SAVING");
            TaskService.global().run(() -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop"));
        });
    }

    public void startServer(CommandSender sender, String name, Consumer<Integer> handler) {
        String apiKey = config().value("api-key").string();
        String daemon = config().value("daemon").string();

        TaskService.async().run(() -> {
            var response = HttpRequest.http(HttpMethod.GET, daemon + "/api/instance")
                    .param("apikey", apiKey)
                    .param("daemonId", this.config().value("daemon-id").string())
                    .param("uuid", name)
                    .browserBehavior(true)
                    .build()
                    .request();

            if (JsonParser.parseString(response).getAsJsonObject().get("data").getAsJsonObject().get("status").getAsInt() != 0) {
                handler.accept(1);
                return;
            }
            MessageAccessor.send(this.language, sender, "wakeup");

            var res = HttpRequest.http(HttpMethod.GET, daemon + "/api/protected_instance/open")
                    .param("apikey", apiKey)
                    .param("daemonId", this.config().value("daemon-id").string())
                    .param("uuid", name)
                    .build()
                    .request();

            this.logger.info("starting instance: " + name);
            if (JsonParser.parseString(res).getAsJsonObject().get("status").getAsInt() == 200) {
                handler.accept(0);
                return;
            } else {
                this.logger.info("starting instance failed: " + response);
            }
            handler.accept(2);
        });
    }

    @QuarkCommand(name = "join-dyn-server", permission = "+quark.dynserver")
    public static final class JoinDynamicServerCommand extends ModuleCommand<MCSMDynamicInstance> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (getConfig().value("instance").bool()) {
                return;
            }
            ConfigurationSection servers = this.getConfig().value("servers").section();
            if (!Objects.requireNonNull(servers).contains(args[0])) {
                MessageAccessor.send(this.getLanguage(), sender, "not-found", args[0]);
                return;
            }

            MessageAccessor.send(this.getLanguage(), sender, "checking");

            try {
                this.getModule().startServer(sender, servers.getString(args[0]), (i) -> {
                    switch (i) {
                        case 0 -> MessageAccessor.send(this.getLanguage(), sender, "starting");
                        case 1 -> MessageAccessor.send(this.getLanguage(), sender, "running");
                        case 2 -> MessageAccessor.send(this.getLanguage(), sender, "start-error");
                    }
                });
            } catch (Exception e) {
                ExceptionUtil.log(e);
                MessageAccessor.send(this.getLanguage(), sender, "start-error");
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.addAll(Objects.requireNonNull(this.getConfig().value("servers").section()).getKeys(false));
            }
        }
    }
}