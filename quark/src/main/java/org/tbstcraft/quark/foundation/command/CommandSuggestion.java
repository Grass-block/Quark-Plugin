package org.tbstcraft.quark.foundation.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.util.CachedInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public final class CommandSuggestion {
    private final CommandSender sender;
    private final String[] buffer;

    private final List<String> suggestions = new ArrayList<>();

    public CommandSuggestion(CommandSender sender, String[] buffer) {
        this.sender = sender;
        this.buffer = buffer;
    }

    private void add(Collection<String> items) {
        suggestions.addAll(items);
    }

    public void suggest(int pos, String... suggestions) {
        if (this.buffer.length - 1 != pos) {
            return;
        }
        this.add(List.of(suggestions));
    }

    public void suggest(int pos, Collection<String> suggestions) {
        if (this.buffer.length - 1 != pos) {
            return;
        }
        this.add(suggestions);
    }

    public void suggestOnlinePlayers(int pos) {
        if (this.buffer.length - 1 != pos) {
            return;
        }
        this.add(Players.getAllOnlinePlayerNames());
    }

    public void suggestPlayers(int pos) {
        if (this.buffer.length - 1 != pos) {
            return;
        }
        this.add(CachedInfo.getAllPlayerNames());
    }


    public List<String> getSuggestions() {
        return suggestions;
    }

    public void requireAnyPermission(Consumer<CommandSuggestion> provider, Permission... permissions) {
        for (Permission permission : permissions) {
            if (this.sender.hasPermission(permission)) {
                provider.accept(this);
                return;
            }
        }
    }

    public void requireAllPermission(Consumer<CommandSuggestion> provider, Permission... permissions) {
        for (Permission permission : permissions) {
            if (!this.sender.hasPermission(permission)) {
                return;
            }
        }
        provider.accept(this);
    }

    public void matchArgument(int position, String item, Consumer<CommandSuggestion> action) {
        if (this.buffer.length - 1 < position) {
            return;
        }
        if (!this.buffer[position].equals(item)) {
            return;
        }
        action.accept(this);
    }

    public CommandSender getSender() {
        return this.sender;
    }

    public Player getSenderAsPlayer() {
        return ((Player) this.sender);
    }
}
