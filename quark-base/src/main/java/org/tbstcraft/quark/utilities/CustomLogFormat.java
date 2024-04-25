package org.tbstcraft.quark.utilities;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.service.data.ModuleDataService;
import org.tbstcraft.quark.util.FilePath;
import org.tbstcraft.quark.util.api.APIProfile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

@QuarkModule(version = "1.0.0", compatBlackList = {APIProfile.BUKKIT, APIProfile.SPIGOT})
@QuarkCommand(name = "log-format", permission = "quark.log-format")
public final class CustomLogFormat extends CommandModule {
    @Override
    public void enable() {
        super.enable();
        this.logger = createLogger();
        NBTTagCompound tag = ModuleDataService.getEntry(this.getId());
        if (!tag.hasKey("enable")) {
            tag.setBoolean("enable", true);
            ModuleDataService.save(this.getId());
        }
        if (tag.getBoolean("enable")) {
            this.setFormat();
        }
    }

    public void setFormat() {
        File f = new File(FilePath.pluginFolder(Quark.PLUGIN_ID) + "/log.xml");
        if (!f.exists() || f.length() == 0) {
            this.restoreFormatFile();
        }
        try {
            this.setLoggerFormat(f.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean setLoggerFormat(URL resource) {
        if (resource == null) {
            return false;
        }
        try {
            InputStream stream = resource.openStream();
            if (stream == null) {
                return false;
            }
            stream.readAllBytes();
            stream.close();
        } catch (IOException e) {
            return false;
        }

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        try {
            context.setConfigLocation(resource.toURI());
        } catch (URISyntaxException e) {
            return false;
        }
        context.reconfigure();
        return true;
    }

    public void restoreFormatFile() {
        File f = new File(FilePath.pluginFolder(Quark.PLUGIN_ID) + "/log.xml");
        this.logger.info("covered log file.");
        FilePath.cover(f, this.getResource("/log.xml"));
    }

    public void setDataEnable(boolean enable) {
        NBTTagCompound tag = ModuleDataService.getEntry(this.getId());
        if (tag.hasKey("enable")) {
            tag.setBoolean("enable", enable);
            ModuleDataService.save(this.getId());
        }
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "reload" -> {
                this.setFormat();
                this.getLanguage().sendMessageTo(sender, "reload");
            }
            case "restore" -> {
                this.restoreFormatFile();
                this.setFormat();
                this.getLanguage().sendMessageTo(sender, "restore");
            }
            case "on" -> {
                this.setDataEnable(true);
                this.setFormat();
                this.getLanguage().sendMessageTo(sender, "enable");
            }
            case "off" -> {
                this.setDataEnable(false);
                if (!this.setLoggerFormat(Bukkit.class.getResource("/log4j2.xml"))) {
                    this.logger.severe("failed to inject log format, consider checking resource.");
                }
                this.getLanguage().sendMessageTo(sender, "disable");
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length != 1) {
            return;
        }
        tabList.add("reload");
        tabList.add("restore");
        tabList.add("on");
        tabList.add("off");
    }
}
