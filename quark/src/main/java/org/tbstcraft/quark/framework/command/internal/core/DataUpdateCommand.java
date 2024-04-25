package org.tbstcraft.quark.framework.command.internal.core;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.framework.command.CoreCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.service.data.ModuleDataService;
import org.tbstcraft.quark.service.data.PlayerDataService;

import java.util.List;

@QuarkCommand(name = "data-backend", permission = "-quark.config.update-data")
public class DataUpdateCommand extends CoreCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]){
            case "level-db"->{
                ModuleDataService.INSTANCE.get().getBackend().convertToDB();
                PlayerDataService.INSTANCE.get().getBackend().convertToDB();
            }
            case "file"->{
                ModuleDataService.INSTANCE.get().getBackend().covertToLegacy();
                PlayerDataService.INSTANCE.get().getBackend().covertToLegacy();
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if(buffer.length==1){
            tabList.add("level-db");
            tabList.add("file");
        }
    }
}
