package org.tbstcraft.quark.security.command;

import org.tbstcraft.quark.SharedContext;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.security.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@QuarkCommand(name = "permission", op = true)
public final class PermissionCommand extends ModuleCommand<PermissionManager> {
    public PermissionCommand(PermissionManager module) {
        super(module);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        String moduleId = this.getModuleId();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            this.getLanguage().sendMessageTo(sender, moduleId, "cmd_player_not_found", args[1]);
            return;
        }
        switch (args[0]) {
            case "permission" -> SharedContext.SHARED_THREAD_POOL.submit(() -> {
                this.getModule().setPermission(target, args[2], Boolean.parseBoolean(args[3]));
                this.getModule().sync(target);
                this.getLanguage().sendMessageTo(sender, moduleId, "cmd_perm_set", args[1], args[2], args[3]);
            });
            case "group" -> SharedContext.SHARED_THREAD_POOL.submit(() -> {
                this.getModule().setPermissionGroup(target, args[2]);
                this.getModule().sync(target);
                this.getLanguage().sendMessageTo(sender, moduleId, "cmd_group_set", args[1], args[2], args[3]);
            });
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
        switch (args.length) {
            case 0 -> {
                tabList.add("permission");
                tabList.add("group");
            }
            case 1 -> {
                switch (args[0]) {
                    case "permission"->{

                    }
                }
            }
        }
    }
}
