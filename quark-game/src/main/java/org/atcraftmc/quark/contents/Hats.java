package org.atcraftmc.quark.contents;

import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.util.List;

@QuarkModule
@QuarkCommand(name = "hat", permission = "+quark.hat.command", playerOnly = true)
public final class Hats extends CommandModule {

    @Inject("-quark.hat.other")
    private Permission setOtherPermission;
    
    @Inject
    private LanguageEntry language;

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ItemStack stack = player.getInventory().getItemInMainHand();

        if (stack.getType().isAir()) {
            this.language.sendMessage(sender, "empty");
            return;
        }

        Player target;

        if (args.length == 0) {
            target = player;
        } else {
            if (!sender.isOp()) {
                sendPermissionMessage(sender);
                return;
            }
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                this.language.sendMessage(sender, "not-found");
                return;
            }
        }

        if (!setHat(target, stack)) {
            this.language.sendMessage(sender, "failed");
        } else {
            this.language.sendMessage(sender, "success");

            player.getInventory().setItemInMainHand(null);
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            if(!sender.isOp()){
                return;
            }
            tabList.addAll(Players.getAllOnlinePlayerNames());
        }
    }

    public boolean setHat(Player player, ItemStack stack) {
        PlayerInventory inv = player.getInventory();
        if (inv.getHelmet() != null) {
            return false;
        }
        inv.setHelmet(stack);
        return true;
    }
}
