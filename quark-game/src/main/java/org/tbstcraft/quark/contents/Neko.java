package org.tbstcraft.quark.contents;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.command.CommandRegistry;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.data.ModuleDataService;
import org.tbstcraft.quark.util.container.CachedInfo;
import org.tbstcraft.quark.util.api.PlayerUtil;
import me.gb2022.commons.nbt.NBTTagCompound;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("deprecation")
@QuarkModule(version = "1.0.0")
@CommandRegistry(Neko.NekoCommand.class)
@EventListener
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

            String fix = this.getLanguage().getMessage(PlayerUtil.getLocale(event.getPlayer()), "chat_postfix");

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
                this.getLanguage().sendMessageTo(sender, "back", args[0]);
                tag.remove(args[0]);
            } else {
                this.getModule().players.add(args[0]);
                this.getLanguage().sendMessageTo(sender, "to", args[0]);
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
