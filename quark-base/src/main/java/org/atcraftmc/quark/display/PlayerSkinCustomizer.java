package org.atcraftmc.quark.display;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;

@SLModule
@QuarkCommand(name = "skin", permission = "+quark.skin")
public class PlayerSkinCustomizer extends CommandModule {
    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireClass(() -> Class.forName("org.bukkit.profile.PlayerProfile"));
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {

    }

    @Override
    public void execute(CommandExecution context) {
        var player = context.requireSenderAsPlayer();
        var profile = player.getPlayerProfile();
        var tex = profile.getTextures();

        profile.setTextures(tex);
        profile.update();
        player.setPlayerProfile(profile);
    }
}
