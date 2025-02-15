package org.atcraftmc.quark.proxy;

import com.google.gson.JsonParser;
import me.gb2022.commons.http.HttpMethod;
import me.gb2022.commons.http.HttpRequest;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.util.ExceptionUtil;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@QuarkModule(defaultEnable = false, version = "1.2")
@AutoRegister({ServiceType.EVENT_LISTEN, ServiceType.REMOTE_MESSAGE})
@CommandProvider(MCSMDynamicInstance.JoinDynamicServerCommand.class)
public final class MCSMDynamicInstance extends PackageModule {
    @Inject
    private Logger logger;
    
    @Override
    public void enable() {
        if (!this.getConfig().getBoolean("instance")) {
            return;
        }
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            this.scheduleStop();
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (!this.getConfig().getBoolean("instance")) {
            return;
        }
        this.logger.info("cancel server stopping process as POWER_SAVING");
        TaskService.async().cancel("quark:ps:countdown");
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (!this.getConfig().getBoolean("instance")) {
            return;
        }
        if (Bukkit.getOnlinePlayers().size() == 1) {
            this.scheduleStop();
        }
    }

    public void scheduleStop() {
        this.logger.info("starting stopping countdown");
        TaskService.async().delay("quark:ps:countdown", getConfig().getInt("shutdown-delay"), () -> {
            this.logger.info("stopping server as POWER_SAVING");
            TaskService.global().run(() -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop"));
        });
    }

    public void startServer(CommandSender sender, String name, Consumer<Integer> handler) {
        String apiKey = getConfig().getString("api-key");
        String daemon = getConfig().getString("daemon");

        TaskService.async().run(() -> {
            var response = HttpRequest.http(HttpMethod.GET, daemon + "/api/instance")
                    .param("apikey", apiKey)
                    .param("daemonId", this.getConfig().getString("daemon-id"))
                    .param("uuid", name)
                    .browserBehavior(true)
                    .build()
                    .request();

            if (JsonParser.parseString(response).getAsJsonObject().get("data").getAsJsonObject().get("status").getAsInt() != 0) {
                handler.accept(1);
                return;
            }
            this.getLanguage().sendMessage(sender, "wakeup");

            var res = HttpRequest.http(HttpMethod.GET, daemon + "/api/protected_instance/open")
                    .param("apikey", apiKey)
                    .param("daemonId", this.getConfig().getString("daemon-id"))
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
            if (getConfig().getBoolean("instance")) {
                return;
            }
            ConfigurationSection servers = this.getConfig().getSection("servers");
            if (!Objects.requireNonNull(servers).contains(args[0])) {
                this.getLanguage().sendMessage(sender, "not-found", args[0]);
                return;
            }

            this.getLanguage().sendMessage(sender, "checking");

            try {
                this.getModule().startServer(sender, servers.getString(args[0]), (i) -> {
                    switch (i) {
                        case 0 -> getLanguage().sendMessage(sender, "starting");
                        case 1 -> getLanguage().sendMessage(sender, "running");
                        case 2 -> getLanguage().sendMessage(sender, "start-error");
                    }
                });
            } catch (Exception e) {
                ExceptionUtil.log(e);
                getLanguage().sendMessage(sender, "start-error");
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.addAll(Objects.requireNonNull(this.getConfig().getSection("servers")).getKeys(false));
            }
        }
    }
}