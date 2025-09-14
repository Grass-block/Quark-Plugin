package org.atcraftmc.starlight.warp;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.starlight.core.data.WaypointService;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.foundation.platform.Players;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.util.BukkitSound;
import org.atcraftmc.starlight.util.PlayerMap;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;


@SLModule(version = "1.3.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(BackToDeath.BackCommand.class)
public final class BackToDeath extends PackageModule implements PluginCommandExecutor {
    private final WaypointService service = new WaypointService("sl_deaths");

    private final PlayerMap<Location> deaths = new PlayerMap<>();


    @Inject("tip-back")
    private LanguageItem tipBack;

    @Override
    public void enable() throws Exception {
        super.enable();
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tipBack));
    }

    @Override
    public void disable() throws Exception {
        super.disable();
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tipBack));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!this.deaths.contains(event.getPlayer())) {
            return;
        }
        MessageAccessor.send(this.getLanguage(), event.getPlayer(), "back-hint");
    }

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent event) {
        var player = event.getEntity();
        this.deaths.put(player, player.getLocation());
    }

    @Override
    public void execute(CommandExecution context) {
        var sender = context.requireSenderAsPlayer();

        if (!this.deaths.contains(sender)) {
            MessageAccessor.send(this.getLanguage(), sender, "back-not-set");
            return;
        }

        Players.teleport(sender, this.deaths.get(sender)).thenAccept((b) -> {
            MessageAccessor.send(this.getLanguage(), sender, "back-tp-success");
            BukkitSound.WARP.play(sender);
            this.deaths.remove(sender);
        });
    }

    @QuarkCommand(name = "back", playerOnly = true, permission = "+quark.warp.back")
    public static final class BackCommand extends ModuleCommand<BackToDeath> {
        @Override
        public void init(BackToDeath module) {
            setExecutor(module);
        }
    }
}
