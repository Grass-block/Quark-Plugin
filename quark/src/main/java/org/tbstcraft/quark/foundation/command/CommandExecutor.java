package org.tbstcraft.quark.foundation.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.command.assertion.ArgumentAssertionException;
import org.tbstcraft.quark.foundation.command.assertion.CommandAssertionException;
import org.tbstcraft.quark.util.ExceptionUtil;

import java.util.ArrayList;
import java.util.List;

public interface CommandExecutor {
    @ApiStatus.Obsolete
    default void onCommand(CommandSender sender, String[] args) {
        try {
            this.execute(new CommandExecution(sender, args));
        } catch (ArgumentAssertionException e) {
            List<Object> lst = new ArrayList<>(e.getInfo().length + 1);
            lst.add(e.getPosition());
            lst.addAll(List.of(e.getInfo()));

            Quark.LANGUAGE.sendMessage(sender, "command", "error-" + e.getCode(), lst.toArray());
        } catch (CommandAssertionException e) {
            Quark.LANGUAGE.sendMessage(sender, "command", "error-" + e.getCode(), e.getInfo());
        } catch (Throwable throwable) {
            Quark.LANGUAGE.sendMessage(sender, "command", "error-internal-exception");
            ExceptionUtil.log(throwable);
        }
    }

    @ApiStatus.Obsolete
    default void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        CommandSuggestion suggestion = new CommandSuggestion(sender, buffer);
        this.suggest(suggestion);
        tabList.addAll(suggestion.getSuggestions());
    }


    default void suggest(CommandSuggestion suggestion) {
    }

    default void execute(CommandExecution context) {
    }
}
