package org.tbstcraft.quark.util.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.data.config.Language;
import org.tbstcraft.quark.util.container.ObjectContainer;
import org.tbstcraft.quark.util.platform.APIProfileTest;

import java.time.Duration;
import java.util.Locale;
import java.util.function.Function;

public interface TextSender {
    ObjectContainer<TextSender> BACKEND = new ObjectContainer<>();

    static void initContext() {
        if (APIProfileTest.isSpigotServer() || APIProfileTest.isArclightBasedServer()) {
            Quark.LOGGER.warning("using custom text backend. this will cause text interaction unavailable.");
            BACKEND.set(new SpigotSender());
            return;
        }
        if (APIProfileTest.isPaperCompat()) {
            BACKEND.set(new DirectSender());
            return;
        }
        BACKEND.set(new BukkitSender());
    }


    static void sendLine(CommandSender sender, ComponentLike component) {
        BACKEND.get().send(sender, component);
    }

    static void sendBlock(CommandSender sender, ComponentBlock block) {
        for (Component line : block) {
            sendLine(sender, line);
        }
    }

    static void sendLine(ComponentLike component) {
        sendLine(Bukkit.getConsoleSender(), component);
    }

    static void sendBlock(ComponentBlock component) {
        sendBlock(Bukkit.getConsoleSender(), component);
    }

    static void broadcastLine(Function<Locale, ComponentLike> component, boolean opOnly, boolean toConsole) {
        BACKEND.get().broadcast(component, opOnly, toConsole);
    }

    static void broadcastBlock(Function<Locale, ComponentBlock> component, boolean opOnly, boolean toConsole) {
        BACKEND.get()._broadcast(component, opOnly, toConsole);
    }

    static void sendTo(CommandSender sender, ComponentBlock block) {
        sendLine(sender, block.toSingleLine());
    }

    static void sendToConsole(ComponentBlock block) {
        for (Component line : block) {
            sendLine(line);
        }
    }


    static void sendToTitle(Player p, ComponentLike component, int titleFadein, int titleStay, int titleFadeout) {
        BACKEND.get().sendTitle(p, component, titleFadein, titleStay, titleFadeout);
    }


    void sendTitle(Player p, ComponentLike component, int titleFadein, int titleStay, int titleFadeout);

    void send(CommandSender sender, ComponentLike component);

    default void broadcast(Function<Locale, ComponentLike> component, boolean opOnly, boolean toConsole) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.isOp() && opOnly) {
                continue;
            }
            send(p, component.apply(Language.locale(p)));
        }
        if (!toConsole) {
            return;
        }
        send(Bukkit.getConsoleSender(), component.apply(Locale.ENGLISH));
    }

    default void _broadcast(Function<Locale, ComponentBlock> component, boolean opOnly, boolean toConsole) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.isOp() && opOnly) {
                continue;
            }
            for (Component c : component.apply(Language.locale(p))) {
                send(p, c);
            }

        }
        if (!toConsole) {
            return;
        }
        for (Component c : component.apply(Locale.ENGLISH)) {
            send(Bukkit.getConsoleSender(), c);
        }

    }

    final class DirectSender implements TextSender {

        @Override
        public void sendTitle(Player p, ComponentLike component, int titleFadein, int titleStay, int titleFadeout) {
            p.showTitle(Title.title(Component.text(""), component.asComponent(), Title.Times.times(
                    Duration.ofMillis(titleFadein * 50L),
                    Duration.ofMillis(titleStay * 50L),
                    Duration.ofMillis(titleFadeout * 50L)
            )));
        }

        @Override
        public void send(CommandSender sender, ComponentLike component) {
            if(sender instanceof ConsoleCommandSender){
                sender.sendMessage(LegacyComponentSerializer.legacySection().serialize(component.asComponent()));
                return;
            }
            sender.sendMessage(component.asComponent());
        }
    }

    final class SpigotSender implements TextSender {
        @Override
        public void send(CommandSender sender, ComponentLike component) {
            sender.spigot().sendMessage(ComponentParser.asBungee(component));
        }

        @Override
        public void sendTitle(Player p, ComponentLike component, int titleFadein, int titleStay, int titleFadeout) {
            p.sendTitle("", ComponentParser.asString(component), titleFadein, titleStay, titleFadeout);
        }

        @Override
        public void broadcast(Function<Locale, ComponentLike> component, boolean opOnly, boolean toConsole) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.isOp() && opOnly) {
                    continue;
                }
                p.sendMessage(ComponentParser.asBungee(component.apply(Language.locale(p))));
            }
            if (!toConsole) {
                return;
            }
            Bukkit.getConsoleSender().sendMessage(ComponentParser.asBungee(component.apply(Locale.ENGLISH)));
        }
    }

    final class BukkitSender implements TextSender {
        @Override
        public void sendTitle(Player p, ComponentLike component, int titleFadein, int titleStay, int titleFadeout) {
            p.sendTitle(LegacyComponentSerializer.legacySection().serialize(component.asComponent()), "", titleFadein, titleStay, titleFadeout);
        }

        @Override
        public void send(CommandSender sender, ComponentLike component) {
            sender.sendMessage(LegacyComponentSerializer.legacySection().serialize(component.asComponent()));
        }
    }
}
