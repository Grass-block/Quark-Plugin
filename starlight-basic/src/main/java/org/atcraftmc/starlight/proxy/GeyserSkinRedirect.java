package org.atcraftmc.starlight.proxy;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

@ApplicationModule(id = "geyser-skin-redirect")
@AutoRegister({Registrations.SERVER_EVENT})
@CommandProvider(GeyserSkinRedirect.RedirectSkinCommand.class)
public class GeyserSkinRedirect extends SLPackageModule implements PluginCommandExecutor {

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireMethod(() -> OfflinePlayer.class.getMethod("getPlayerProfile"));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.redirect(event.getPlayer());
    }

    public void redirect(Player player) {
        var prefix = this.config().value("prefix").string();

        if (!player.getName().startsWith(prefix)) {
            return;
        }

        TaskService.async().run(() -> {
            this.handle().getLogger().info("redirecting player {}", player.getName());

            var profile = player.getPlayerProfile();
            var source = Bukkit.getOfflinePlayer(player.getName().substring(prefix.length())).getPlayerProfile();

            profile.setTextures(source.getTextures());

            TaskService.global().run(() -> player.setPlayerProfile(profile));

            this.handle().getLogger().info("redirected player {} to {}", player.getName(), source.getId());
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
