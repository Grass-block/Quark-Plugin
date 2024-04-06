package org.tbstcraft.quark.display;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.config.Queries;
import org.tbstcraft.quark.module.CommandModule;
import org.tbstcraft.quark.module.services.EventListener;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.service.data.ModuleDataService;
import org.tbstcraft.quark.text.TextBuilder;
import org.tbstcraft.quark.util.CachedInfo;
import org.tbstcraft.quark.util.api.APIProfileTest;
import org.tbstcraft.quark.util.api.PlayerUtil;
import me.gb2022.commons.nbt.NBTTagCompound;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("deprecation")
@EventListener
@QuarkModule
@QuarkCommand(name = "header", op = true)
public final class PlayerNameHeader extends CommandModule {
    @Override
    public void enable() {
        super.enable();
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.attach(p);
        }
    }

    @Override
    public void disable() {
        super.disable();
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.detach(p);
        }
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player p = PlayerUtil.strictFindPlayer(args[1]);
        NBTTagCompound entry = ModuleDataService.getEntry(this.getId());
        if (Objects.equals(args[0], "set")) {
            args[2] = args[2] + "{}";
            entry.setString(args[1], args[2]);
            this.getLanguage().sendMessageTo(sender, "set-header", args[1], args[2]);
        }
        if (Objects.equals(args[0], "clear")) {
            entry.remove(args[1]);
            this.getLanguage().sendMessageTo(sender, "clear-header", args[1]);

        }
        if (p != null && p.isOnline()) {
            this.attach(p);
        }
        ModuleDataService.save(this.getId());
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("set");
            tabList.add("clear");
        }
        if (buffer.length == 2) {
            tabList.addAll(CachedInfo.getAllPlayerNames());
        }
        if (buffer.length == 3 && Objects.equals(buffer[0], "set")) {
            tabList.add("<header>");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.attach(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        this.detach(event.getPlayer());
    }

    public void attach(Player p) {
        Component name = getPlayerName(p);
        if (APIProfileTest.isPaperCompat()) {
            p.playerListName(name);
            p.customName(name);
            p.displayName(name);
            return;
        }
        String _name = LegacyComponentSerializer.legacySection().serialize(name);
        p.setDisplayName(_name);
        p.setPlayerListName(_name);
        p.setCustomName(_name);
        p.setCustomNameVisible(true);
    }

    public void detach(Player p) {
        p.setDisplayName(p.getName());
        p.setPlayerListName(p.getName());
        p.setCustomName(null);
    }

    public Component getPlayerName(Player player) {
        String header;
        String name = player.getName();
        NBTTagCompound tag = ModuleDataService.getEntry("player-name-header");
        if (tag.hasKey(name)) {
            header = tag.getString(name);
        } else {
            if (player.isOp()) {
                header = getLanguage().getMessage("zh_cn", "op-header");
            } else {
                header = this.getLanguage().getMessage("zh_cn", "player-header");
            }
        }
        String template = this.getConfig().getString("template");
        if (template == null) {
            return Component.text(player.getName());
        }
        return TextBuilder.buildComponent(
                Queries.GLOBAL_TEMPLATE_ENGINE.handle(
                        template.replace("{player}", player.getName()).replace("{header}", header + "{}")));

    }
}
