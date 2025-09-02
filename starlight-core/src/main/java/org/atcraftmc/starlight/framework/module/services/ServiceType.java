package org.atcraftmc.starlight.framework.module.services;

public interface ServiceType {
    String EVENT_LISTEN = Registers.BUKKIT_EVENT;
    String REMOTE_MESSAGE = Registers.APM_EVENT;
    String PLUGIN_MESSAGE = Registers.PLUGIN_MESSAGE;
    String CLIENT_MESSAGE = "qb:cm";
    String REMOTE_MESSAGE_LISTENER = Registers.APM_LISTEN;
}
