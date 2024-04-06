package org.tbstcraft.quark.tweaks;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.CommandModule;
import org.tbstcraft.quark.module.QuarkModule;

import java.util.List;
import java.util.Objects;


@QuarkModule(version = "1.1.0")
@QuarkCommand(name = "flyspeed", playerOnly = true)
public final class FlySpeedModifier extends CommandModule {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player p) {
            if (Objects.equals(args[0], "reset")) {
                p.setFlySpeed(0.125f);
                this.getLanguage().sendMessageTo(sender, "cmd-speed-set", "0.125");
                return;
            }
            float speed = Float.parseFloat(args[0]);
            if (speed < 0.0f || speed > 1.0f) {
                this.sendExceptionMessage(sender);
                return;
            }
            p.setFlySpeed(speed);
            this.getLanguage().sendMessageTo(sender, "cmd-speed-set", Float.toString(speed));
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length != 1) {
            return;
        }
        tabList.add("0.0625");
        tabList.add("0.03125");
        tabList.add("0.125");
        tabList.add("0.25");
        tabList.add("0.5");
        tabList.add("1");
        tabList.add("reset");
    }
}
