package org.tbstcraft.quark.foundation.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import me.gb2022.commons.container.ObjectContainer;

import java.time.Duration;
import java.util.Locale;
import java.util.function.Function;

public interface TextSender {
    ObjectContainer<TextSender> BACKEND = new ObjectContainer<>();

    static void initContext() {
        if (APIProfileTest.isSpigotServer() || APIProfileTest.isArclightBasedServer()) {
            Quark.getInstance().getLogger().warning("using custom text backend. this will cause text interaction unavailable.");
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

    static void title(Player p, ComponentLike component, int in, int stay, int out) {
        BACKEND.get().sendTitle(p, component, in, stay, out);
    }

    static void fullTitle(Player p, ComponentLike title, ComponentLike subtitle, int in, int stay, int out) {
        BACKEND.get().sendFullTitle(p, title, subtitle, in, stay, out);
    }

    static void subtitle(Player p, ComponentLike component, int in, int stay, int out) {
        BACKEND.get().sendSubtitle(p, component, in, stay, out);
    }

    void sendSubtitle(Player p, ComponentLike component, int in, int stay, int out);

    void sendFullTitle(Player p, ComponentLike title, ComponentLike subtitle, int in, int stay, int out);

    void sendTitle(Player p, ComponentLike component, int in, int stay, int out);

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

        private Title.Times time(int in, int stay, int out) {
            return Title.Times.times(
                    Duration.ofMillis(in * 50L),
                    Duration.ofMillis(stay * 50L),
                    Duration.ofMillis(out * 50L)
            );
        }

        @Override
        public void sendFullTitle(Player p, ComponentLike title, ComponentLike subtitle, int in, int stay, int out) {
            p.showTitle(Title.title(title.asComponent(), subtitle.asComponent(), time(in, stay, out)));
        }

        @Override
        public void sendTitle(Player p, ComponentLike component, int in, int stay, int out) {
            p.showTitle(Title.title(component.asComponent(), Component.text(""), time(in, stay, out)));
        }

        @Override
        public void sendSubtitle(Player p, ComponentLike component, int in, int stay, int out) {
            p.showTitle(Title.title(Component.text(""), component.asComponent(), time(in, stay, out)));
        }

        @Override
        public void send(CommandSender sender, ComponentLike component) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(LegacyComponentSerializer.legacySection().serialize(component.asComponent()));
                return;
            }
            sender.sendMessage(component.asComponent());
        }
    }

    final class SpigotSender implements TextSender {
        @Override
        public void send(CommandSender sender, ComponentLike component) {
            sender.spigot().sendMessage(ComponentSerializer.bungee(component));
        }

        @Override
        public void sendFullTitle(Player p, ComponentLike title, ComponentLike subtitle, int in, int stay, int out) {
            p.sendTitle(ComponentSerializer.legacy(title), ComponentSerializer.legacy(subtitle), in, stay, out);
        }

        @Override
        public void sendSubtitle(Player p, ComponentLike component, int in, int stay, int out) {
            p.sendTitle("", ComponentSerializer.legacy(component), in, stay, out);
        }

        @Override
        public void sendTitle(Player p, ComponentLike component, int in, int stay, int out) {
            p.sendTitle(ComponentSerializer.legacy(component), "", in, stay, out);
        }

        @Override
        public void broadcast(Function<Locale, ComponentLike> component, boolean opOnly, boolean toConsole) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.isOp() && opOnly) {
                    continue;
                }
                p.sendMessage(ComponentSerializer.bungee(component.apply(Language.locale(p))));
            }
            if (!toConsole) {
                return;
            }
            Bukkit.getConsoleSender().sendMessage(ComponentSerializer.bungee(component.apply(Locale.ENGLISH)));
        }
    }

    final class BukkitSender implements TextSender {
        @Override
        public void sendFullTitle(Player p, ComponentLike title, ComponentLike subtitle, int in, int stay, int out) {
            p.sendTitle(ComponentSerializer.legacy(title), ComponentSerializer.legacy(subtitle), in, stay, out);
        }

        @Override
        public void sendSubtitle(Player p, ComponentLike component, int in, int stay, int out) {
            p.sendTitle("", ComponentSerializer.legacy(component), in, stay, out);
        }

        @Override
        public void sendTitle(Player p, ComponentLike component, int in, int stay, int out) {
            p.sendTitle(ComponentSerializer.legacy(component), "", in, stay, out);
        }

        @Override
        public void send(CommandSender sender, ComponentLike component) {
            sender.sendMessage(LegacyComponentSerializer.legacySection().serialize(component.asComponent()));
        }
    }
}
