package org.atcraftmc.quark.display;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import me.gb2022.modular.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.SLCommandModule;
import me.gb2022.modular.module.ApplicationModule;

@ApplicationModule
@QuarkCommand(name = "skin", permission = "+quark.skin")
public class PlayerSkinCustomizer extends SLCommandModule {
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
