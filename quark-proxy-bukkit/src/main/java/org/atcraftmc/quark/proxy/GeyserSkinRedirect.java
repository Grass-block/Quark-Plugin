package org.atcraftmc.quark.proxy;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommandExecutor;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.UUID;

@QuarkModule
@AutoRegister({ServiceType.EVENT_LISTEN})
@CommandProvider(GeyserSkinRedirect.RedirectSkinCommand.class)
public class GeyserSkinRedirect extends PackageModule implements QuarkCommandExecutor {

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireMethod(() -> OfflinePlayer.class.getMethod("getPlayerProfile"));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.redirect(event.getPlayer());
    }

    public void redirect(Player player) {
        var prefix = this.getConfig().getString("prefix");

        if (!player.getName().startsWith(prefix)) {
            return;
        }

        TaskService.async().run(() -> {
            getL4jLogger().info("redirecting player {}", player.getName());

            var profile = player.getPlayerProfile();
            var source = Bukkit.getOfflinePlayer(player.getName().substring(prefix.length())).getPlayerProfile();

            profile.setTextures(source.getTextures());

            TaskService.global().run(() -> player.setPlayerProfile(profile));

            getL4jLogger().info("redirected player {} to {}", player.getName(), source.getId());
        });
    }

    @Override
    public void execute(CommandExecution context) {
        redirect(context.requirePlayer(0));
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggestPlayers(0);
    }

    @QuarkCommand(name = "redirect-be-skin", permission = "-quark.be.redirectskin")
    public static class RedirectSkinCommand extends ModuleCommand<GeyserSkinRedirect> {
        @Override
        public void init(GeyserSkinRedirect module) {
            setExecutor(module);
        }
    }
}
