package org.tbstcraft.quark.display;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.service.base.task.TaskService;
import org.tbstcraft.quark.command.CommandRegistry;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.data.PlayerDataService;
import org.tbstcraft.quark.framework.text.TextBuilder;
import org.tbstcraft.quark.framework.text.TextSender;
import org.tbstcraft.quark.util.api.PlayerUtil;
import me.gb2022.commons.nbt.NBTTagCompound;

@EventListener
@CommandRegistry({WelcomeMessage.WelcomeMessageCommand.class})
@QuarkModule(version = "0.1.0")
public class WelcomeMessage extends PackageModule {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String id = event.getPlayer().getName();
        NBTTagCompound tag = PlayerDataService.getEntry(id, this.getId());
        if (tag.hasKey("join")) {
            return;
        }
        tag.setInteger("join", 0);
        PlayerDataService.save(id);
        TaskService.laterTask(5, () -> this.sendWelcomeMessage(event.getPlayer()));
    }

    private void sendWelcomeMessage(Player player) {
        String msg = this.getLanguage().buildUI(this.getConfig(), "ui", PlayerUtil.getLocale(player));
        msg = msg.replace("{player}", player.getName());
        TextSender.sendLine(player,TextBuilder.buildComponent(msg));
    }

    @QuarkCommand(name = "welcome-message")
    public static final class WelcomeMessageCommand extends ModuleCommand<WelcomeMessage> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.getModule().sendWelcomeMessage(((Player) sender));
        }
    }
}
