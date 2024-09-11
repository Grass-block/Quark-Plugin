package org.atcraftmc.quark.proxysupport;

import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteMessageEvent;
import me.gb2022.apm.remote.event.remote.RemoteQueryEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import me.gb2022.commons.reflect.AutoRegister;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.RemoteMessageService;

@QuarkModule
@AutoRegister(ServiceType.REMOTE_MESSAGE)
public final class ServerStatementObserver extends PackageModule {
    @Override
    public void enable() {
        RemoteMessageService.broadcast("/quark/observe/online", (b) -> {
            String id = RemoteMessageService.getInstance().getMessenger().getConnector().getIdentifier();
            BufferUtil.writeString(b, id);
        });
    }

    @Override
    public void disable() {
        RemoteMessageService.broadcast("/quark/observe/offline", (b) -> {
            String id = RemoteMessageService.getInstance().getMessenger().getConnector().getIdentifier();
            BufferUtil.writeString(b, id);
        });
    }

    @RemoteEventHandler("/quark/observe/online")
    public void onServerOnline(RemoteMessageEvent event) {
        this.getLanguage().broadcastMessage(false, false, "online", BufferUtil.readString(event.getData()));
    }

    @RemoteEventHandler("/quark/observe/offline")
    public void onServerOffline(RemoteMessageEvent event) {
        this.getLanguage().broadcastMessage(false, false, "offline", BufferUtil.readString(event.getData()));
    }

    @RemoteEventHandler("/quark/observe/query")
    public void onServerQuery(RemoteQueryEvent event) {
        event.writeResult((b) -> b.writeByte(114));
    }
}
