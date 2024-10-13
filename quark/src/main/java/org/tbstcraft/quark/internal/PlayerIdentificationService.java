package org.tbstcraft.quark.internal;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.config.ConfigEntry;
import org.tbstcraft.quark.framework.service.*;
import org.tbstcraft.quark.util.Identifiers;

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
            return Identifiers.internal(player.getUniqueId().toString());
        }

        @Override
        public String transform(String playerName) {
            if (playerName.matches("^[a-f\\d]{4}(?:[a-f\\d]{4}_){4}[a-f\\d]{12}$")) {
                Quark.LOGGER.warning("cannot transform an UUID.");
                return playerName;
            }


            var uuid = Bukkit.getPlayerUniqueId(playerName);

            if (uuid == null) {
                Quark.LOGGER.warning("Player " + playerName + " cannot have a valid UUID.");
                return Identifiers.internal(playerName);
            }

            return Identifiers.internal(uuid.toString());
        }
    }
}
