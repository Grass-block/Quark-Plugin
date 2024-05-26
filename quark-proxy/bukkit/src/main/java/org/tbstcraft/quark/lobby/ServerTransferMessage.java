package org.tbstcraft.quark.lobby;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ModuleService;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.util.BukkitSound;

import java.util.Objects;

@ModuleService(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.0")
@QuarkCommand(name = "transfer-display", playerOnly = true)
public class ServerTransferMessage extends CommandModule {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (Objects.equals(args[0], "#dev")) {
            this.getLanguage().sendMessageTo(sender, "server_in_dev");
            BukkitSound.DENY.play(((Player) sender));
            return;
        }
        this.getLanguage().sendMessageTo(sender, "server_transfer", toServerDisplayName(args[0]));
        BukkitSound.WARP.play((Player) sender);
    }

    private String toServerDisplayName(String name) {
        ConfigurationSection map = Objects.requireNonNull(this.getConfig().getConfigurationSection("name_mapping"));
        if (!map.contains(name)) {
            return name;
        }
        return map.getString(name);
    }
}
