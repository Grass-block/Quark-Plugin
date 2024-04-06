package org.tbstcraft.quark.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.service.audience.AudienceService;
import org.tbstcraft.quark.util.ObjectContainer;
import org.tbstcraft.quark.util.api.APIProfileTest;

import java.time.Duration;

public interface TextSender {
    ObjectContainer<TextSender> BACKEND = new ObjectContainer<>();

    static void initContext() {
        if (APIProfileTest.isSpigotServer()||APIProfileTest.isArclightBasedServer()) {
            Quark.LOGGER.warning("using custom text backend. this will cause text interaction unavailable.");
            BACKEND.set(new SpigotSender());
            return;
        }
        BACKEND.set(new AudienceBasedSender());
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

    static void broadcastLine(ComponentLike component,boolean opOnly, boolean toConsole) {
        BACKEND.get().broadcast(component, opOnly, toConsole);
    }

    static void broadcastBlock(ComponentBlock component,boolean opOnly,boolean toConsole){
        for (Component line : component) {
            broadcastLine(component, opOnly, toConsole);
        }
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

    void broadcast(ComponentLike component, boolean opOnly, boolean toConsole);

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
        public void broadcast(ComponentLike component, boolean opOnly, boolean toConsole) {
            for (Player p:Bukkit.getOnlinePlayers()){
                if(!p.isOp()&&opOnly){
                    continue;
                }
                p.sendMessage(ComponentParser.asBungee(component));
            }
            Bukkit.getConsoleSender().sendMessage(ComponentParser.asBungee(component));
        }
    }

    final class AudienceBasedSender implements TextSender {

        @Override
        public void sendTitle(Player p, ComponentLike component, int titleFadein, int titleStay, int titleFadeout) {
            AudienceService.getPlayer(p).showTitle(Title.title(Component.text(""), component.asComponent(), Title.Times.times(
                    Duration.ofMillis(titleFadein * 50L),
                    Duration.ofMillis(titleStay * 50L),
                    Duration.ofMillis(titleFadeout * 50L)
            )));
        }

        @Override
        public void send(CommandSender sender, ComponentLike component) {
            if(sender instanceof ConsoleCommandSender s){
                s.sendMessage(LegacyComponentSerializer.legacySection().serialize((Component) component));
                return;
            }
            AudienceService.ofSender(sender).sendMessage(component);
        }

        @Override
        public void broadcast(ComponentLike component, boolean opOnly, boolean toConsole) {
            if(opOnly) {
                AudienceService.getOperators().sendMessage(component);
            }else{
                AudienceService.getPlayers().sendMessage(component);
            }
            if(toConsole){
                AudienceService.getConsole().sendMessage(component);
            }
        }
    }
}
