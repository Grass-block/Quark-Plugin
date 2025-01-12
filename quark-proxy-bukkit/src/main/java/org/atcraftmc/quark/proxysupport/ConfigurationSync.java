package org.atcraftmc.quark.proxysupport;

import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.util.FilePath;

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
