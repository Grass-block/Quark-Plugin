package org.atcraftmc.starlight.management;

import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
@SLModule(version = "2.0.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(Maintenance.MaintenanceCommand.class)
public final class Maintenance extends PackageModule {
    private final Set<UUID> allowed = new HashSet<>();
    private boolean active = false;

    @Inject("-starlight.maintenance.bypass;false")
    private Permission bypass;

    @Override
    public void enable() {
        for (var p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission(this.bypass)) {
                continue;
            }

            this.allowed.add(p.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission(this.bypass)) {
            return;
        }

        this.allowed.add(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!this.active) {
            return;
        }
        var p = Bukkit.getOfflinePlayer(event.getName()).getPlayer();
        var locale = MinecraftLocale.ZH_CN;

        if (p != null) {
            locale = LocaleService.locale(p);
            if (this.allowed.contains(p.getUniqueId())) {
                return;
            }
        }

        var message = MessageAccessor.getMessage(this.getLanguage(), LocaleService.locale(p), "kick-message");
        var name = event.getPlayerProfile().getName();

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, PluginMessenger.queryKickMessage(name, message, locale.minecraft()));
    }

    public void kickAll() {
        for (var player : Bukkit.getOnlinePlayers()) {
            if (this.allowed.contains(player.getUniqueId())) {
                continue;
            }

            var msg = MessageAccessor.getMessage(this.getLanguage(), LocaleService.locale(player), "kick-message");
            player.kickPlayer(msg);
        }
    }

    @QuarkCommand(name = "maintenance", permission = "-quark.maintenance.command")
    public static final class MaintenanceCommand extends ModuleCommand<Maintenance> {

        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, "allow", "disallow");
            if (this.getModule().active) {
                suggestion.suggest(0, "off");
            } else {
                suggestion.suggest(0, "on");
            }

            suggestion.matchArgument(0, "allow", (ctx) -> ctx.suggestPlayers(1));
            suggestion.matchArgument(
                    0,
                    "disallow",
                    (ctx) -> ctx.suggest(1, getModule().allowed.stream().map(UUID::toString).collect(Collectors.toSet()))
            );
        }

        @Override
        public void execute(CommandExecution context) {
            var sender = context.getSender();

            switch (context.requireEnum(0, "allow", "disallow", "on", "off")) {
                case "allow" -> {
                    var player = context.requireOfflinePlayer(1);
                    this.getModule().allowed.add(player.getUniqueId());
                    MessageAccessor.send(this.getLanguage(), sender, "allow", player.getName(), player.getUniqueId());
                }
                case "disallow" -> {
                    var uuid = UUID.fromString(context.requireArgumentAt(1));
                    this.getModule().allowed.remove(uuid);
                    MessageAccessor.send(this.getLanguage(), sender, "disallow", uuid);
                }
                case "enable" -> {
                    MessageAccessor.send(this.getLanguage(), sender, "start");
                    this.getModule().active = true;
                    this.getModule().kickAll();
                }
                case "disable" -> {
                    MessageAccessor.send(this.getLanguage(), sender, "end");
                    this.getModule().active = false;
                }
                default -> this.sendExceptionMessage(sender);
            }
        }
    }
}
