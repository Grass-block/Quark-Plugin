package org.tbstcraft.quark.util;

import me.gb2022.commons.nbt.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public interface NBTExaminer {
    static String toString(NBTTagCompound tag, int deep) {
        var builder = new StringBuilder();

        for (String s : tag.getTagMap().keySet()) {
            var b = tag.getTag(s);
            builder.append("\n").append(" ".repeat(deep * 2));
            if (b instanceof NBTTagByte) {
                builder.append("[&bB&f]").append(s).append("&7=&f").append(tag.getByte(s));
            }
            if (b instanceof NBTTagShort) {
                builder.append("[&bS&f]").append(s).append("&7=&f").append(tag.getShort(s));
            }
            if (b instanceof NBTTagInt) {
                builder.append("[&bI&f]").append(s).append("&7=&f").append(tag.getInteger(s));
            }
            if (b instanceof NBTTagLong) {
                builder.append("[&bL&f]").append(s).append("&7=&f").append(tag.getLong(s));
            }
            if (b instanceof NBTTagFloat) {
                builder.append("[&bF&f]").append(s).append("&7=&f").append(tag.getFloat(s));
            }
            if (b instanceof NBTTagDouble) {
                builder.append("[&bD&f]").append(s).append("&7=&f").append(tag.getDouble(s));
            }
            if (b instanceof NBTTagString) {
                builder.append("[&aS&f]").append(s).append("&7=&f").append(tag.getString(s));
            }
            if (b instanceof NBTTagCompound tc) {
                builder.append("[&dC&f]").append(s);
                builder.append(toString(tc, deep + 1));
            }
        }

        return builder.toString();
    }

    static void send(CommandSender sender, NBTTagCompound tag) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', toString(tag, 0)));
    }

    static void toConsole(NBTTagCompound tag) {
        send(Bukkit.getConsoleSender(), tag);
    }
}
