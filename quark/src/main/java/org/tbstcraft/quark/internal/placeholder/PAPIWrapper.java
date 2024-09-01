package org.tbstcraft.quark.internal.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.Quark;

public interface PAPIWrapper {
    static PAPIWrapper getInstance() {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Quark.getInstance().getLogger().info("using PlaceholderAPI text formatter");
            return new Impl();
        } catch (Exception e) {
            Quark.getInstance().getLogger().info("using no-PlaceholderAPI text formatter.");
            return new FallbackImpl();
        }
    }

    String handle(String input);

    String handlerPlayer(Player player, String input);

    final class Impl implements PAPIWrapper {
        @Override
        public String handle(String input) {
            try {
                return PlaceholderAPI.setPlaceholders(null, input);
            } catch (Exception ignored) {
                return input;
            }
        }

        @Override
        public String handlerPlayer(Player player, String input) {
            try {
                return PlaceholderAPI.setPlaceholders(player, input);
            } catch (Exception ignored) {
                return input;
            }
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
