package org.tbstcraft.quark.proxysupport;

import me.gb2022.commons.nbt.NBT;
import me.gb2022.commons.nbt.NBTTagCompound;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.service.proxy.ChannelHandler;
import org.tbstcraft.quark.service.proxy.ProxyChannel;
import org.tbstcraft.quark.service.proxy.ProxyMessageService;
import org.tbstcraft.quark.util.FilePath;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Objects;

public class ConfigurationSync extends PackageModule implements ChannelHandler {
    public static final String SYNC_CACHE_FOLDER = FilePath.pluginFolder(Quark.PLUGIN_ID) + "/sync-cache";

    @Override
    public void enable() {
        ProxyMessageService.addMessageHandler("quark:sync.vars", this);
    }

    @Override
    public void onMessageReceived(String channelId, byte[] data, ProxyChannel channel) {
        if (Objects.equals(channelId, "quark:sync.vars")) {
            try {
                NBTTagCompound tag = (NBTTagCompound) NBT.readZipped(new ByteArrayInputStream(data));
            } catch (Exception e) {
                return;
            }

            File f=new File(SYNC_CACHE_FOLDER+"/global-vars.dat");

            if(f.getParentFile().mkdirs()){
                getLogger().info("created sync folder");
            }

        }
    }

    @QuarkCommand(name="sync-data",subCommands = {GlobalVariablesSyncCommand.class})
    public static final class SyncDataCommand extends ModuleCommand<ConfigurationSync>{

    }

    @QuarkCommand(name = "global-vars")
    public static final class GlobalVariablesSyncCommand extends ModuleCommand<ConfigurationSync>{

    }

}
