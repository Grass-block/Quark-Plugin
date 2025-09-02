package org.atcraftmc.starlight.console;

import me.gb2022.apm.local.PluginMessenger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.starlight.Configurations;
import org.atcraftmc.starlight.foundation.platform.APIProfile;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

@SLModule(version = "1.0.0", defaultEnable = false, compatBlackList = {APIProfile.BUKKIT, APIProfile.SPIGOT, APIProfile.ARCLIGHT})
@QuarkCommand(name = "log-format", permission = "-starlight.console.format")
public final class CustomLogFormat extends CommandModule {

    @Override
    public void enable() {
        try {
            this.setLoggerFormat(Configurations.file("log.xml", false).toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disable() {
        this.setLoggerFormat(Bukkit.class.getResource("/log4j2.xml"));
    }

    public void setLoggerFormat(URL resource) {
        if (resource == null) {
            return;
        }
        try {
            InputStream stream = resource.openStream();
            if (stream == null) {
                return;
            }
            stream.readAllBytes();
            stream.close();
        } catch (IOException e) {
            return;
        }

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        try {
            context.setConfigLocation(resource.toURI());
        } catch (URISyntaxException e) {
            return;
        }
        context.reconfigure();
        PluginMessenger.broadcastListed("starlight:log:reconfigure", List.of());
    }
}
