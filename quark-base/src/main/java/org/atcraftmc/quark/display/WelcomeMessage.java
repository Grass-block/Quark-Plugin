package org.atcraftmc.quark.display;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import net.kyori.adventure.text.ComponentLike;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.atcraftmc.starlight.data.PlayerDataService;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.TaskService;

@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider({WelcomeMessage.WelcomeMessageCommand.class})
@SLModule(version = "0.1.0")
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
        try {
            var msg = this.getLanguage().inline(Language.generateTemplate(this.getConfig(), "ui"), LocaleService.locale(player));
            msg = msg.replace("{player}", player.getName());
            ComponentLike component = TextBuilder.buildComponent(msg);
            TextSender.sendMessage(player, component);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @QuarkCommand(name = "welcome-message")
    public static final class WelcomeMessageCommand extends ModuleCommand<WelcomeMessage> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.getModule().sendWelcomeMessage(((Player) sender));
        }
    }
}
