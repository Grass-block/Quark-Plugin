package org.tbstcraft.quark.deprecated.audience_service;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.foundation.platform.Players;

import java.util.function.Predicate;

public abstract class AudienceProvider implements AudienceService {
    public final Audience filter(Predicate<Audience> filter) {
        return all().filterAudience(filter);
    }

    @Override
    public final ForwardingAudience players() {
        return players(Bukkit.getOnlinePlayers());
    }

    @Override
    public final ForwardingAudience operators() {
        return players(Players.getOnlinePlayers(Player::isOp));
    }

    @Override
    public final Audience serverOperators() {
        return Audience.audience(operators(), console());
    }

    @Override
    public final Audience player(String name) {
        return player(Bukkit.getPlayerExact(name));
    }

    @Override
    public final Audience all() {
        return Audience.audience(players(), console());
    }

    @Override
    public final Audience sender(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return console();
        }
        return player(((Player) sender));
    }

    public void close() {
    }
}
