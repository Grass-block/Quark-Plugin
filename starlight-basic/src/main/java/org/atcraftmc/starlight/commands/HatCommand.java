package org.atcraftmc.starlight.commands;

import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.foundation.platform.Players;
import org.atcraftmc.starlight.framework.module.SLCommandModule;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.util.List;

@ApplicationModule(id = "hat")
@QuarkCommand(name = "hat", permission = "+starlight.hat", playerOnly = true)
public final class HatCommand extends SLCommandModule {

    @Inject("-starlight.hat.other")
    private Permission setOtherPermission;

    @Inject
    private LanguageEntry language;

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ItemStack stack = player.getInventory().getItemInMainHand();

        if (stack.getType().isAir()) {
            MessageAccessor.send(this.language, sender, "empty");
            return;
        }

        Player target;

        if (args.length == 0) {
            target = player;
        } else {
            if (!sender.hasPermission(setOtherPermission)) {
                sendPermissionMessage(sender);
                return;
            }
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                MessageAccessor.send(this.language, sender, "not-found");
                return;
            }
        }

        if (!setHat(target, stack)) {
            MessageAccessor.send(this.language, sender, "failed");
        } else {
            MessageAccessor.send(this.language, sender, "success");

            player.getInventory().setItemInMainHand(null);
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            if (!sender.isOp()) {
                return;
            }
            tabList.addAll(Players.getAllOnlinePlayerNames());
        }
    }

    public boolean setHat(Player player, ItemStack stack) {
        var inv = player.getInventory();
        if (inv.getHelmet() != null) {
            return false;
        }
        inv.setHelmet(stack);
        return true;
    }
}
