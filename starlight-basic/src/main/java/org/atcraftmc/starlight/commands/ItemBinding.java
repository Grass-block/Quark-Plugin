package org.atcraftmc.starlight.commands;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.Registrations;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.core.custom.CustomMeta;
import me.gb2022.modular.module.ApplicationModule;

import java.util.List;
import java.util.Objects;

@ApplicationModule(id="item-command",version = "0.3")
@AutoRegister(Registrations.SERVER_EVENT)
@CommandProvider({ItemBinding.ItemCommandCommand.class})
public final class ItemBinding extends SLPackageModule implements PluginCommandExecutor {

    @Inject("tip")
    private LanguageItem tip;

    @Inject
    private LanguageEntry language;

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
            MessageAccessor.send(this.language, sender, "bind-failed");
            return;
        }
        String id = stack.getType().getKey().getKey();
        if (args[0].equals("none")) {
            CustomMeta.removeItemPDCProperty(stack, "cmd_bind");
            MessageAccessor.send(this.language, sender, "unbind", id);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : args) {
                sb.append(s).append(" ");
            }
            String cmdLine = sb.toString();

            CustomMeta.setItemPDCProperty(stack, "cmd_bind", cmdLine);
            MessageAccessor.send(this.language, sender, "bind", id, cmdLine);
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
    public static final class ItemCommandCommand extends ModuleCommand<ItemBinding> {
        @Override
        public void init(ItemBinding module) {
            this.setExecutor(module);
        }
    }
}
