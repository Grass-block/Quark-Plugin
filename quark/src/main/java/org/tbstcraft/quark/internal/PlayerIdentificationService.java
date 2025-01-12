package org.tbstcraft.quark.internal;

import org.bukkit.entity.Player;
import org.tbstcraft.quark.Quark;
import org.atcraftmc.qlib.config.ConfigEntry;
import org.tbstcraft.quark.framework.service.*;
import org.tbstcraft.quark.util.Identifiers;

import java.util.Objects;

@QuarkService(id = "hashing")
public interface PlayerIdentificationService extends Service {

    @RegisterAsGlobal
    @ServiceInject
    ServiceHolder<PlayerIdentificationService> INSTANCE = new ServiceHolder<>();

    @ServiceProvider
    static PlayerIdentificationService create(ConfigEntry config) {
        return new UUIDTransformer();
    }

    static String transformPlayer(Player player) {
        return INSTANCE.get().transform(player);
    }

    static String transformID(String player) {
        return INSTANCE.get().transform(player);
    }


    String transform(String playerName);

    String transform(Player player);


    final class NameTransformer implements PlayerIdentificationService {
        @Override
        public String transform(Player player) {
            return Identifiers.internal(player.getName());
        }

        @Override
        public String transform(String playerName) {
            return Identifiers.internal(playerName);
        }
    }

    final class UUIDTransformer implements PlayerIdentificationService {
        @Override
        public String transform(Player player) {
            return Identifiers.internal(Objects.requireNonNull(player.getPlayerProfile().getUniqueId()).toString());
        }

        @Override
        public String transform(String playerName) {
            if (playerName.matches("^[a-f\\d]{4}(?:[a-f\\d]{4}_){4}[a-f\\d]{12}$")) {
                Quark.LOGGER.warn("cannot transform an UUID: {}", playerName);
                return playerName;
            }

            Quark.LOGGER.warn("Player {} cannot have a valid UUID.", playerName);

            return Identifiers.internal(playerName);
        }
    }
}
