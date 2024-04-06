package org.tbstcraft.quark.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.command.CommandRegistry;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.services.EventListener;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.util.api.BukkitUtil;

import java.util.List;
import java.util.Objects;

@QuarkModule(version = "0.3")
@EventListener
@CommandRegistry({ItemCommand.ItemCommandCommand.class})
public class ItemCommand extends PackageModule {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        ItemStack hand = event.getItem();
        if (hand == null) {
            return;
        }

        String usage=BukkitUtil.getItemUsage(hand);
        if(!usage.startsWith("cmd-bind->")){
            return;
        }
        Bukkit.getServer().dispatchCommand(p, usage.split("->")[1]);
        event.setCancelled(true);
    }

    @QuarkCommand(name = "item-command", playerOnly = true)
    public static final class ItemCommandCommand extends ModuleCommand<ItemCommand> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            ItemStack stack = ((Player) sender).getInventory().getItemInMainHand();
            if (stack.getType() == Material.AIR) {
                this.getLanguage().sendMessageTo(sender, "bind-failed");
                return;
            }
            String id = stack.getType().getKey().getKey();
            if (args[0].equals("none")) {
                BukkitUtil.setItemUsage(stack, null);
                this.getLanguage().sendMessageTo(sender, "unbind", id);
            } else {
                StringBuilder sb = new StringBuilder();
                for (String s : args) {
                    sb.append(s).append(" ");
                }
                String cmdLine = sb.toString();

                if(!Objects.equals(BukkitUtil.getItemUsage(stack), "")){
                    BukkitUtil.setItemUsage(stack, null);
                }
                BukkitUtil.setItemUsage(stack, "cmd-bind->" + cmdLine);
                this.getLanguage().sendMessageTo(sender, "bind", id, cmdLine);
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("none");
                tabList.add("<command line>");
            }
        }
    }
}
