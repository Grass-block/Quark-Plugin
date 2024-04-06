package org.tbstcraft.quark.lobby;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.CommandModule;
import org.tbstcraft.quark.module.QuarkModule;

import java.util.List;
import java.util.Objects;

@QuarkCommand(name = "npc-chat")
@QuarkModule(version = "1.0.0")
public class NPCChat extends CommandModule {

    @Override
    public void onCommand(CommandSender sender, String[] args) {


    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if(buffer.length==1){
            for (String s: Objects.requireNonNull(this.getConfig().getConfigurationSection("npc")).getKeys(false)){
                tabList.add(s);
            }
        }
    }
}
