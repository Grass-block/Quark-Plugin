package org.atcraftmc.quark.display;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.Bukkit;
import org.bukkit.profile.PlayerProfile;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.net.MalformedURLException;
import java.net.URL;

@QuarkModule
@QuarkCommand(name = "skin",permission = "+quark.skin")
public class PlayerSkinCustomizer extends CommandModule {
    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireClass(() -> Class.forName("org.bukkit.profile.PlayerProfile"));
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {

    }

    @SuppressWarnings("removal")
    @Override
    public void execute(CommandExecution context) {
        var player = context.requireSenderAsPlayer();
        var profile = player.getPlayerProfile();
        var tex = profile.getTextures();

        var arg=context.requireArgumentAt(0);

        PlayerProfile p = Bukkit.createProfile(arg);

        profile.setTextures(null);
        //profile.update();
        player.setPlayerProfile(profile);
    }
}
