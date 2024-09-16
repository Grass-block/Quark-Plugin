package org.atcraftmc.quark.display;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.foundation.text.TextSender;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider({WelcomeMessage.WelcomeMessageCommand.class})
@QuarkModule(version = "0.1.0")
public final class WelcomeMessage extends PackageModule {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String id = event.getPlayer().getName();
        NBTTagCompound tag = PlayerDataService.getEntry(id, this.getId());
        if (tag.hasKey("join")) {
            return;
        }
        tag.setInteger("join", 0);
        PlayerDataService.save(id);
        TaskService.global().delay(5, () -> this.sendWelcomeMessage(event.getPlayer()));
    }

    private void sendWelcomeMessage(Player player) {
        String msg = this.getLanguage().buildTemplate(Language.locale(player), Language.generateTemplate(this.getConfig(), "ui"));
        msg = msg.replace("{player}", player.getName());
        TextSender.sendLine(player, TextBuilder.buildComponent(msg));
    }

    @QuarkCommand(name = "welcome-message")
    public static final class WelcomeMessageCommand extends ModuleCommand<WelcomeMessage> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.getModule().sendWelcomeMessage(((Player) sender));
        }
    }
}
