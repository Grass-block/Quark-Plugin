package org.atcraftmc.quark.tweaks;

import me.gb2022.commons.reflect.Inject;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.util.List;
import java.util.Objects;


@QuarkModule(version = "1.2.0")
@CommandProvider({FlySpeedModifier.FlySpeedCommand.class, FlySpeedModifier.FlyToggleCommand.class})
public final class FlySpeedModifier extends PackageModule {
    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
    }

    @Override
    public void disable(){
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
    }

    @QuarkCommand(name = "flyspeed", permission = "+quark.fly.flyspeed", playerOnly = true)
    public static final class FlySpeedCommand extends ModuleCommand<FlySpeedModifier> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (sender instanceof Player p) {
                if (Objects.equals(args[0], "reset")) {
                    p.setFlySpeed(0.125f);
                    this.getLanguage().sendMessage(sender, "cmd-speed-set", "0.125");
                    return;
                }
                float speed = Float.parseFloat(args[0]);
                if (speed < 0.0f || speed > 1.0f) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                p.setFlySpeed(speed);
                this.getLanguage().sendMessage(sender, "cmd-speed-set", Float.toString(speed));
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

    @QuarkCommand(name = "fly", permission = "-quark.fly.toggle", playerOnly = true)
    public static final class FlyToggleCommand extends ModuleCommand<FlySpeedModifier> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.getLanguage().sendMessage(sender, "toggle");
            Player p = ((Player) sender);
            p.setAllowFlight(!p.getAllowFlight());
        }
    }

}
