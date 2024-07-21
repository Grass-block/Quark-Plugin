package org.tbstcraft.quark.management;

import org.bukkit.BanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.foundation.command.CommandManager;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.util.container.CachedInfo;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

@QuarkModule(version = "1.0.0")
@QuarkCommand(name = "ban")
public final class AdvancedBan extends CommandModule {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("minecraft.command.ban")) {
            this.sendPermissionMessage(sender);
            return;
        }

        Calendar calender = Calendar.getInstance();

        switch (args[2]) {
            case "forever" -> calender.set(9999, Calendar.DECEMBER, 31, 23, 59, 59);
            case "until" -> {
                calender.set(Calendar.YEAR, Integer.parseInt(args[3]));
                calender.set(Calendar.MONTH, Integer.parseInt(args[4]) - 1);
                calender.set(Calendar.DATE, Integer.parseInt(args[5]));
                calender.set(Calendar.HOUR, Integer.parseInt(args[6]) - 12);
                calender.set(Calendar.MINUTE, Integer.parseInt(args[7]));
                calender.set(Calendar.SECOND, Integer.parseInt(args[8]));
            }
            case "time" -> {
                calender.add(Calendar.YEAR, Integer.parseInt(args[3]));
                calender.add(Calendar.MONTH, Integer.parseInt(args[4]));
                calender.add(Calendar.DATE, Integer.parseInt(args[5]));
                calender.add(Calendar.HOUR, Integer.parseInt(args[6]));
                calender.add(Calendar.MINUTE, Integer.parseInt(args[7]));
                calender.add(Calendar.SECOND, Integer.parseInt(args[8]));
            }
        }

        String player = args[0];
        String reason = args[1];

        PlayerUtil.banPlayer(player, BanList.Type.NAME, reason, calender.getTime(), sender.getName());

        String date = SharedObjects.DATE_FORMAT.format(calender.getTime());

        this.getLanguage().sendMessage(sender, "msg-ban-complete",
                player,
                date,
                reason
        );

        if (this.getConfig().getBoolean("broadcast")) {
            this.getLanguage().broadcastMessage(false, false, "broadcast", player, reason, sender.getName(), date);
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        switch (buffer.length) {
            case 1 -> tabList.addAll(CachedInfo.getAllPlayerNames());
            case 2 -> tabList.add("[reason]");
            case 3 -> {
                tabList.add("until");
                tabList.add("time");
                tabList.add("forever");
                return;
            }
        }
        if (buffer.length <= 3) {
            return;
        }
        if (Objects.equals(buffer[2], "forever")) {
            return;
        }
        switch (buffer.length) {
            case 4 -> tabList.add("[year]");
            case 5 -> tabList.add("[month]");
            case 6 -> tabList.add("[day]");
            case 7 -> tabList.add("[hour]");
            case 8 -> tabList.add("[minute]");
            case 9 -> tabList.add("[second]");
        }
    }

    @Override
    public Command getCoveredCommand() {
        return CommandManager.getCommandMap().getCommand("minecraft:ban");
    }
}
