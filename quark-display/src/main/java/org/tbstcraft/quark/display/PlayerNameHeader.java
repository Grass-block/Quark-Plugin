package org.tbstcraft.quark.display;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.CommandModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.service.ModuleDataService;
import org.tbstcraft.quark.util.BukkitUtil;
import org.tbstcraft.quark.util.nbt.NBTTagCompound;

import java.util.List;
import java.util.Objects;

@QuarkModule
@QuarkCommand(name = "header", op = true)
public class PlayerNameHeader extends CommandModule {
    @Override
    public void onEnable() {
        super.onEnable();
        this.registerListener();
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.attach(p);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.unregisterListener();
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.detach(p);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        Player p = Bukkit.getPlayer(args[1]);
        NBTTagCompound entry = ModuleDataService.getEntry(this.getId());
        if (Objects.equals(args[0], "set")) {
            entry.setString(args[1], args[2]);
            this.getLanguage().sendMessageTo(sender, "set_header", args[1], args[2]);
        }
        if (Objects.equals(args[0], "clear")) {
            entry.remove(args[1]);
            this.getLanguage().sendMessageTo(sender, "clear_header", args[1]);

        }
        if (p != null && p.isOnline()) {
            this.attach(p);
        }
        ModuleDataService.save(this.getId());
        return true;
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
        if (args.length == 1) {
            tabList.add("set");
            tabList.add("clear");
        }
        if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                tabList.add(p.getName());
            }
        }
        if (args.length == 3 && Objects.equals(args[0], "set")) {
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
        String name = getPlayerName(p);
        p.setPlayerListName(name);
        p.setCustomName(name);
        p.setCustomNameVisible(true);
        p.setDisplayName(name);
    }

    public void detach(Player p) {
        p.setDisplayName(p.getName());
        p.setPlayerListName(p.getName());
        p.setCustomName(null);
        p.setCustomNameVisible(false);
    }

    public String getPlayerName(Player player) {
        String header;
        String name = player.getName();
        NBTTagCompound tag = ModuleDataService.getEntry("player_name_header");
        if (tag.hasKey(name)) {
            header = tag.getString(name);
        } else {
            if (player.isOp()) {
                header = getLanguage().getMessage("zh_cn", "op_header");
            } else {
                header = this.getLanguage().getMessage("zh_cn", "player_header");
            }
        }
        String template = this.getConfig().getString("template");
        if (template == null) {
            return player.getName();
        }
        return BukkitUtil.formatChatComponent(template.replace("{player}", player.getName())
                .replace("{header}", header));
    }
}
