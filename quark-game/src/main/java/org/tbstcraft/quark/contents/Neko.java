package org.tbstcraft.quark.contents;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.data.ModuleDataService;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.util.CachedInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("deprecation")
@QuarkModule(version = "1.0.0")
@CommandProvider(Neko.NekoCommand.class)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class Neko extends PackageModule {
    private final Set<String> players = new HashSet<>();

    @Override
    public void enable() {
        this.players.clear();
        this.players.addAll(ModuleDataService.getEntry(this.getFullId()).getTagMap().keySet());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (this.players.contains(event.getPlayer().getName())) {

            String fix = this.getLanguage().getMessage(Language.locale(event.getPlayer()), "chat_postfix");

            event.setMessage(event.getMessage().trim() + fix);
        }
    }

    @QuarkCommand(name = "neko", op = true)
    public static final class NekoCommand extends ModuleCommand<Neko> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            NBTTagCompound tag = ModuleDataService.getEntry(this.getModule().getFullId());

            if (this.getModule().players.contains(args[0])) {
                this.getModule().players.remove(args[0]);
                this.getLanguage().sendMessage(sender, "back", args[0]);
                tag.remove(args[0]);
            } else {
                this.getModule().players.add(args[0]);
                this.getLanguage().sendMessage(sender, "to", args[0]);
                tag.setByte(args[0], (byte) 0);
            }
            ModuleDataService.save(this.getModule().getFullId());
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length != 1) {
                return;
            }
            tabList.addAll(CachedInfo.getAllPlayerNames());
        }
    }
}
