package org.atcraftmc.quark.warps;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommandExecutor;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.util.BukkitSound;

import java.util.HashMap;
import java.util.Map;


@QuarkModule(version = "1.3.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(BackToDeath.BackCommand.class)
public final class BackToDeath extends PackageModule implements QuarkCommandExecutor {
    private final Map<String, Location> deathPoints = new HashMap<>();

    @Inject("tip-back")
    private LanguageItem tipBack;

    @Override
    public void enable() {
        super.enable();
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tipBack));
    }

    @Override
    public void disable() {
        super.disable();
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tipBack));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!this.deathPoints.containsKey(event.getPlayer().getName())) {
            return;
        }
        this.getLanguage().sendMessage(event.getPlayer(), "back-hint");
    }

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent event) {
        var player = event.getEntity();
        this.deathPoints.put(player.getName(), player.getLocation());
    }

    @Override
    public void execute(CommandExecution context) {
        var sender = context.getSender();

        if (!this.deathPoints.containsKey(sender.getName())) {
            this.getLanguage().sendMessage(sender, "back-not-set");
            return;
        }

        Players.teleport(((Player) sender), this.deathPoints.get(sender.getName()));
        this.getLanguage().sendMessage(sender, "back-tp-success");
        BukkitSound.WARP.play(((Player) sender));
    }

    @QuarkCommand(name = "back", playerOnly = true, permission = "+quark.warp.back")
    public static final class BackCommand extends ModuleCommand<BackToDeath> {
        @Override
        public void init(BackToDeath module) {
            setExecutor(module);
        }
    }
}
