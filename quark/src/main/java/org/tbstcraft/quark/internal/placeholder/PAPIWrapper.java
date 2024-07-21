package org.tbstcraft.quark.internal.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.Quark;

public interface PAPIWrapper {
    static PAPIWrapper getInstance() {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Quark.LOGGER.info("using PlaceholderAPI text formatter");
            return new Impl();
        } catch (Exception e) {
            Quark.LOGGER.info("using no-PlaceholderAPI text formatter.");
            return new FallbackImpl();
        }
    }

    String handle(String input);

    String handlerPlayer(Player player, String input);

    final class Impl implements PAPIWrapper {
        @Override
        public String handle(String input) {
            return PlaceholderAPI.setPlaceholders(null, input);
        }

        @Override
        public String handlerPlayer(Player player, String input) {
            return PlaceholderAPI.setPlaceholders(player, input);
        }
    }

    final class FallbackImpl implements PAPIWrapper {
        @Override
        public String handle(String input) {
            return input;
        }

        @Override
        public String handlerPlayer(Player player, String input) {
            return input;
        }
    }
}
