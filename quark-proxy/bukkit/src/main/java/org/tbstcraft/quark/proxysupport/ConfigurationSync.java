package org.tbstcraft.quark.proxysupport;

import me.gb2022.commons.nbt.NBT;
import me.gb2022.commons.nbt.NBTTagCompound;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.service.proxy.ChannelHandler;
import org.tbstcraft.quark.service.proxy.ProxyChannel;
import org.tbstcraft.quark.service.proxy.ProxyMessageService;
import org.tbstcraft.quark.util.FilePath;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Objects;

public class ConfigurationSync extends PackageModule{
    public static final String SYNC_CACHE_FOLDER = FilePath.pluginFolder(Quark.PLUGIN_ID) + "/sync-cache";

    @Override
    public void enable() {
    }


    @QuarkCommand(name="sync-data",subCommands = {GlobalVariablesSyncCommand.class})
    public static final class SyncDataCommand extends ModuleCommand<ConfigurationSync>{

    }

    @QuarkCommand(name = "global-vars")
    public static final class GlobalVariablesSyncCommand extends ModuleCommand<ConfigurationSync>{

    }

}
