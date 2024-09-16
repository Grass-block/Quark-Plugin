package org.atcraftmc.quark.chat;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.command.assertion.NumberLimitation;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.ModuleDataService;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.util.CachedInfo;

import java.util.Date;
import java.util.Objects;

@CommandProvider({ChatMute.MuteCommand.class, ChatMute.UnmuteCommand.class})
@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(id = "chat-mute", version = "1.0.2")
public final class ChatMute extends PackageModule implements Listener {

    @EventHandler
    public void onChatting(AsyncPlayerChatEvent event) {
        this.checkEvent(event.getPlayer(), event);
    }

    @EventHandler
    public void detectCommand(PlayerCommandPreprocessEvent event) {
        if (!(event.getMessage().contains("say") || event.getMessage().contains("tell"))) {
            return;
        }
        this.checkEvent(event.getPlayer(), event);
    }

    public void checkEvent(Player p, Cancellable event) {
        NBTTagCompound tag = ModuleDataService.getEntry(this.getId());
        if (!tag.hasKey(p.getName())) {
            return;
        }

        var expire = tag.getLong(p.getName());
        var expired = SharedObjects.DATE_FORMAT.format(new Date(expire));

        if (expire != 0 && System.currentTimeMillis() > expire) {
            return;
        }

        if (expire == 0) {
            expired = "9999-12-31 23:59:59";
        }

        event.setCancelled(true);
        this.getLanguage().sendMessage(Objects.requireNonNull(p.getPlayer()), "message-banned", expired);
    }


    @QuarkCommand(name = "unmute", permission = "-quark.unmute")
    public static final class UnmuteCommand extends ModuleCommand<ChatMute> {
        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, ModuleDataService.getEntry(this.getModuleId()).getTagMap().keySet());
        }

        @Override
        public void execute(CommandExecution context) {
            var sender = context.getSender();
            var target = context.requireArgumentAt(0);

            var tag = ModuleDataService.getEntry(this.getModuleId());
            if (tag.hasKey(target)) {
                tag.remove(target);
            }
            ModuleDataService.save(this.getModuleId());
            this.getLanguage().sendMessage(sender, "remove", target);

            var player = Bukkit.getPlayerExact(target);
            if (player != null && !target.equals(context.getSender().getName())) {
                this.getLanguage().sendMessage(player, "remove-target", sender.getName());
            }
        }
    }


    @QuarkCommand(name = "mute", op = true)
    public static final class MuteCommand extends ModuleCommand<ChatMute> {

        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, CachedInfo.getAllPlayerNames());
            suggestion.suggest(1, "time[seconds]", "forever");
            suggestion.suggest(2, "<reason>");
        }

        @Override
        public void execute(CommandExecution context) {
            var length = 0;
            var target = context.requireArgumentAt(0);
            var reason = context.requireRemainAsParagraph(2, true);

            if (!Objects.equals(context.requireArgumentAt(1), "forever")) {
                length = context.requireArgumentInteger(1, NumberLimitation.moreThan(0));
            }

            var expire = length != 0 ? System.currentTimeMillis() + length * 1000L : 0;
            var expired = SharedObjects.DATE_FORMAT.format(new Date(expire));

            if (expire == 0) {
                expired = "9999-12-31 23:59:59";
            }

            ModuleDataService.getEntry(this.getModuleId()).hasKey(target);
            this.getLanguage().sendMessage(context.getSender(), "add", target, expired, reason);
            Player player = Bukkit.getPlayerExact(target);
            NBTTagCompound tag = ModuleDataService.getEntry(this.getModuleId());
            if (!tag.hasKey(target)) {
                tag.setLong(target, expire);
            }
            ModuleDataService.save(this.getModuleId());
            if (player != null && !target.equals(context.getSender().getName())) {
                this.getLanguage().sendMessage(player, "add-target", context.getSender().getName(), reason, expired);
            }
        }
    }
}