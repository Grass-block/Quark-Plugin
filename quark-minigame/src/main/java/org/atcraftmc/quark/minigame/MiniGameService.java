package org.atcraftmc.quark.minigame;

import org.tbstcraft.quark.foundation.command.AbstractCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.ServiceHolder;
import org.tbstcraft.quark.framework.service.ServiceInject;

@QuarkService(id = "mini-game")
public interface MiniGameService {
    @ServiceInject
    ServiceHolder<MiniGameService> INSTANCE = new ServiceHolder<>();


    static void registerGame(String id,Class) {
    }

    class Impl implements MiniGameService {

    }

    @QuarkCommand(name = "game", permission = "-quark.operator.minigame")
    class MiniGameCommand extends AbstractCommand {

    }
}
