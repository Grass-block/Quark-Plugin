package org.tbstcraft.quark.misc;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;

import java.util.List;
import java.util.Objects;

@QuarkModule
public class FlySpeedModifier extends PluginModule {
    private final FlySpeedCommand command = new FlySpeedCommand(this);

    @Override
    public void onEnable() {
        this.registerCommand(this.command);
    }

    @Override
    public void onDisable() {
        this.unregisterCommand(this.command);
    }

    @QuarkCommand(name = "flyspeed")
    private static final class FlySpeedCommand extends ModuleCommand<FlySpeedModifier> {
        private FlySpeedCommand(FlySpeedModifier module) {
            super(module);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (sender instanceof Player p) {
                if (Objects.equals(args[0], "reset")) {
                    p.setFlySpeed(0.25f);
                    this.getLanguage().sendMessageTo(sender, "cmd_speed_set", "0.25");
                    return;
                }
                float speed = Float.parseFloat(args[0]);
                if (speed < 0.0f || speed > 1.0f) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                p.setFlySpeed(speed);
                this.getLanguage().sendMessageTo(sender, "cmd_speed_set", Float.toString(speed));
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
            if (args.length != 1) {
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
}
