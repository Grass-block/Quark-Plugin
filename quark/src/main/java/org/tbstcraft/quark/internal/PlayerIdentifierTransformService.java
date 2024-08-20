package org.tbstcraft.quark.internal;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.framework.service.*;

@QuarkService(id = "hashing")
public interface PlayerIdentifierTransformService extends Service {

    @RegisterAsGlobal
    @ServiceInject
    ServiceHolder<PlayerIdentifierTransformService> INSTANCE = new ServiceHolder<>();

    @ServiceProvider
    static PlayerIdentifierTransformService create() {
        if (Bukkit.getOnlineMode()) {
            return new UUIDTransformer();
        }
        return new NameTransformer();
    }

    @ServiceInject
    static void start() {
    }

    @ServiceInject
    static void stop() {
    }


    String transform(Player player);

    final class NameTransformer implements PlayerIdentifierTransformService {
        @Override
        public String transform(Player player) {
            return player.getName();
        }
    }

    final class UUIDTransformer implements PlayerIdentifierTransformService {
        @Override
        public String transform(Player player) {
            return player.getUniqueId().toString();
        }
    }
}
