package org.tbstcraft.quark.internal;

import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceInject;
import org.tbstcraft.quark.internal.command.InternalCommands;

@QuarkService(id = "internal-command-provider")
public interface InternalCommandsProvider extends Service {
    @ServiceInject
    static void start() {
        InternalCommands.register();
    }

    @ServiceInject
    static void stop() {
        InternalCommands.unregister();
    }
}
