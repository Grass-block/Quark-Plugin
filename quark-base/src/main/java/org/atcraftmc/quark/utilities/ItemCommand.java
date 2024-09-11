package org.atcraftmc.quark.utilities;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.execute.CommandExecutor;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.customcontent.CustomMeta;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.List;
import java.util.Objects;

@QuarkModule(version = "0.3")
@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider({ItemCommand.ItemCommandCommand.class})
public final class ItemCommand extends PackageModule implements CommandExecutor {

    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
    }

    @Override
    public void disable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
    }

    @Override
    public void checkCompatibility() {
        Compatibility.requirePDC();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        ItemStack hand = event.getItem();
        if (hand == null) {
            return;
        }

        if (!CustomMeta.hasItemPDCProperty(hand, "cmd_bind")) {
            return;
        }

        String cmd = Objects.requireNonNull(CustomMeta.getItemPDCProperty(hand, "cmd_bind"));

        event.setCancelled(true);

        if (cmd.contains("dm open")) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd + " " + event.getPlayer().getName());
            return;
        }
        Bukkit.getServer().dispatchCommand(p, cmd);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        ItemStack stack = ((Player) sender).getInventory().getItemInMainHand();
        if (stack.getType() == Material.AIR) {
            this.getLanguage().sendMessage(sender, "bind-failed");
            return;
        }
        String id = stack.getType().getKey().getKey();
        if (args[0].equals("none")) {
            CustomMeta.removeItemPDCProperty(stack, "cmd_bind");
            this.getLanguage().sendMessage(sender, "unbind", id);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : args) {
                sb.append(s).append(" ");
            }
            String cmdLine = sb.toString();

            CustomMeta.setItemPDCProperty(stack, "cmd_bind", cmdLine);
            this.getLanguage().sendMessage(sender, "bind", id, cmdLine);
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("none");
            tabList.add("<command line>");
        }
    }

    @QuarkCommand(name = "item-command", playerOnly = true)
    public static final class ItemCommandCommand extends ModuleCommand<ItemCommand> {
        @Override
        public void init(ItemCommand module) {
            this.setExecutor(module);
        }
    }
}
