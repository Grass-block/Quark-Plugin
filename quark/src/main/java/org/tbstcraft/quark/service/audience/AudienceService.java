package org.tbstcraft.quark.service.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
//import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.service.Service;
import org.tbstcraft.quark.util.ObjectContainer;
import org.tbstcraft.quark.util.api.APIProfileTest;

import java.util.Collection;
import java.util.function.Predicate;

public interface AudienceService extends Service {
    ObjectContainer<AudienceProvider> BACKEND = new ObjectContainer<>();


    static void init() {
        BACKEND.set(create(Quark.PLUGIN));
    }

    static void stop() {
        BACKEND.get().close();
    }

    static Audience filtered(Predicate<Audience> filter) {
        return BACKEND.get().filter(filter);
    }

    static ForwardingAudience getPlayers() {
        return BACKEND.get().players();
    }

    static ForwardingAudience getOperators() {
        return BACKEND.get().operators();
    }

    static Audience getServerOperators() {
        return BACKEND.get().serverOperators();
    }

    static Audience getAll() {
        return BACKEND.get().all();
    }

    static ForwardingAudience ofPlayers(Collection<? extends Player> players) {
        return BACKEND.get().players(players);
    }

    static Audience getPlayer(Player player) {
        return BACKEND.get().player(player);
    }

    static Audience getPlayer(String name) {
        return BACKEND.get().player(name);
    }

    static Audience getConsole() {
        return BACKEND.get().console();
    }


    static AudienceProvider create(Plugin plugin) {
        if (APIProfileTest.isPaperCompat()) {
            return new DirectAudienceProvider();
        }
        return new WrappedAudienceProvider(BukkitAudiences.create(plugin));
    }

    static Audience ofSender(CommandSender sender) {
        return BACKEND.get().sender(sender);
    }

    Audience sender(CommandSender sender);

    Audience filter(Predicate<Audience> filter);

    ForwardingAudience players();

    ForwardingAudience operators();

    Audience serverOperators();

    Audience player(String name);

    Audience all();

    ForwardingAudience players(Collection<? extends Player> players);

    Audience player(Player player);

    Audience console();
}
