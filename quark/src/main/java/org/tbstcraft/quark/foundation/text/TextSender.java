package org.tbstcraft.quark.foundation.text;

import me.gb2022.commons.reflect.method.Assertion;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO1;
import me.gb2022.commons.reflect.method.MethodHandleO3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.joml.Vector3i;
import org.tbstcraft.quark.data.language.Language;

import java.time.Duration;
import java.util.Locale;
import java.util.function.Function;

@SuppressWarnings("Convert2MethodRef")
public interface TextSender {
    Assertion ADVENTURE_TITLE_API = () -> Player.class.getMethod("showTitle", Title.class);

    MethodHandleO1<CommandSender, ComponentLike> SEND_MESSAGE = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Player.class.getMethod("sendMessage", Component.class), (p, c) -> p.sendMessage(c));
        ctx.attempt(
                () -> CommandSender.Spigot.class.getMethod("sendMessage", BaseComponent.class),
                (p, c) -> p.spigot().sendMessage(ComponentSerializer.bungee(c))
                   );
        ctx.attempt(
                () -> CommandSender.class.getMethod("sendMessage", String.class),
                (p, c) -> p.sendMessage(ComponentSerializer.legacy(c))
                   );
    });
    MethodHandleO3<Player, ComponentLike, ComponentLike, Vector3i> SEND_TITLE = MethodHandle.select((ctx) -> {
        ctx.attempt(ADVENTURE_TITLE_API, (p, t, s, v) -> {
            var in = Duration.ofMillis(v.x() * 50L);
            var stay = Duration.ofMillis(v.y() * 50L);
            var out = Duration.ofMillis(v.z() * 50L);
            var time = Title.Times.times(in, stay, out);

            p.showTitle(Title.title(t.asComponent(), s.asComponent(), time));
        });
        ctx.dummy((p, t, s, v) -> {
            var title = ComponentSerializer.legacy(t);
            var subtitle = ComponentSerializer.legacy(s);

            p.sendTitle(title, subtitle, v.x(), v.y(), v.z());
        });
    });
    MethodHandleO1<Player, ComponentLike> SEND_ACTIONBAR_TITLE = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Player.class.getMethod("sendActionBar", Component.class), (p, c) -> p.sendActionBar(c));
        ctx.attempt(() -> Player.class.getMethod("sendActionBar", BaseComponent[].class), (p, c) -> {
            var bc = ComponentSerializer.bungee(c);
            p.sendActionBar(bc);
        });
        ctx.attempt(() -> Player.class.getMethod("spigot"), (p, c) -> {
            var bc = ComponentSerializer.bungee(c);
            p.sendMessage(ChatMessageType.ACTION_BAR, bc);
        });
    });


    //message
    static void sendMessage(CommandSender sender, ComponentLike message) {
        SEND_MESSAGE.invoke(sender, message);
    }

    static void sendBlock(CommandSender sender, ComponentBlock block) {
        for (Component line : block) {
            sendMessage(sender, line);
        }
    }

    static void sendMessage(ComponentLike component) {
        CommandSender sender = Bukkit.getConsoleSender();
        sendMessage(sender, component);
    }

    static void sendBlock(ComponentBlock component) {
        sendBlock(Bukkit.getConsoleSender(), component);
    }


    //title
    static void sendTitle(Player viewer, ComponentLike title, ComponentLike subtitle, int in, int stay, int out) {
        SEND_TITLE.invoke(viewer, title, subtitle, new Vector3i(in, stay, out));
    }

    static void title(Player p, ComponentLike component, int in, int stay, int out) {
        sendTitle(p, component, Component.text(""), in, stay, out);
    }

    static void subtitle(Player p, ComponentLike component, int in, int stay, int out) {
        sendTitle(p, Component.text(""), component, in, stay, out);
    }

    static void sendActionbarTitle(Player p, ComponentLike c) {
        SEND_ACTIONBAR_TITLE.invoke(p, c.asComponent());
    }


    //broadcast
    static void broadcastLine(Function<Locale, ComponentLike> component, boolean opOnly, boolean toConsole) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.isOp() && opOnly) {
                continue;
            }
            sendMessage(p, component.apply(Language.locale(p)));
        }
        if (!toConsole) {
            return;
        }
        CommandSender sender = Bukkit.getConsoleSender();
        sendMessage(sender, component.apply(Locale.ENGLISH));
    }

    static void broadcastBlock(Function<Locale, ComponentBlock> component, boolean opOnly, boolean toConsole) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.isOp() && opOnly) {
                continue;
            }
            sendBlock(p, component.apply(Language.locale(p)));
        }
        if (!toConsole) {
            return;
        }
        sendBlock(Bukkit.getConsoleSender(), component.apply(Locale.ENGLISH));
    }


    //misc
    static void sendChatColor(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }
}
